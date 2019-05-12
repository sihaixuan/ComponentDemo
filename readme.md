微信公众号：史莱克learner
欢迎关注，学习技术和投稿，一起学习进步



在将业务进行模块化时，避免不了模块页面路由和模块通信，大多数我们会用到ARouter,EventBus三方库。模块化过程中有一个尴尬的问题摆在面前：Event事件、Router path放在哪里？因为很多业务module都需要收发Event事件,进行页面路由，所以只能将Event，Router path下沉到基础库。



这样导致的结果是基础库越来越大，至多 把Event事件、Router path摆放在独立的module,然后基础库依赖这个库，如下图所示：








我们希望业务模块发送的事件，注解使用的Router path都在模块自己这里定义，而不是下层到基础库,当其他module需要路由、事件、接口就暴露出来。



关于这点《微信Android模块化架构重构实践》也提到了这件事情，并且自创性的使用了一种叫“.api”化的方式来解决这件事情。原理是在编译期将公用接口下沉到基础库同层级，供其他module使用，而这段代码的维护仍然放到非基础库中。这种base库不会膨胀，代码维护的责任制更明确，确定挺不错。如下图：







在ModuleA，B把XXXBusEvents、XXXRouterParams,暴露的公用接口文件后缀名以.api（并不要求一定.api后者，只要跟后续的自动Api化插件或者脚本一致就行）命名，rebuild之后自动生成ModuleA-api，ModuleB-api 模块，ModuleA，B也会自动添加各自对应 api module依赖。



讲完了原理，下面就可以实现，这里使用ARouter,EventBus，只对Java文件进行Api化，步骤如下：



新建工程，创建base、moduleA、moduleB 模块在moudleA,moduleB中创建api文件





默认情况下，Android stuio 是不能识别.api文件，如果想编辑.api后缀的java文件，为了能让Android Studio继续高亮该怎么办？可以在File Type中把.api作为java文件类型，操作如下图：







设置好后，可以在.api文件中像java文件一样愉快撸代码了，其他类可以引用.api中的类。



查看setting.gradle文件脚本如下：

1include ':app', ':base',':modulea',':moduleb'


include 4个module,做个测试，在setting.gradle include test,同步后，test目录下只有iml文件，没有build.gradle、AndroidManifest.xml等文件,所以除了拷贝.api文件到对应目录并重命名为.java，还需要额外创建这两个文件，这里我事先在base module中准备了通用module的build.gradle文件，拷贝到对应目录即可,AndroidManifest.xml就拷贝base module目录下的，脚本实现如下：



 1def includeWithApi(String moduleName,String baseModuleName) {
 2    //先正常加载这个模块
 3    include(moduleName)
 4
 5    //找到这个模块的路径
 6    String originDir = project(moduleName).projectDir
 7    //这个是新的路径
 8    String targetDir = "${originDir}-api"
 9    //新模块的路径
10    def sdkName = "${project(moduleName).name}-api"
11    //新模块名字
12    String apiName="${moduleName.substring(1,moduleName.length())}-api"
13
14
15    //这个是公共模块的位置，我预先放了一个 ApiBuildGralde.gradle 文件进去
16    String apiGradle = project(baseModuleName).projectDir
17
18    // 每次编译删除之前的文件
19    deleteDir(targetDir)
20
21    //复制.api文件到新的路径
22    copy() {
23        from originDir
24        into targetDir
25        exclude '**/build/'
26        exclude '**/res/'
27        include '**/*.api'
28    }
29
30    //直接复制公共模块的AndroidManifest文件到新的路径，作为该模块的文件
31    copy() {
32        from "${apiGradle}/src/main/AndroidManifest.xml"
33        into "${targetDir}/src/main/"
34    }
35
36    //file("${targetDir}/src/main/java/com/dhht/${apiName}/").mkdirs()
37
38    //修改AndroidManifest文件
39    //fileReader("${targetDir}/src/main/AndroidManifest.xml",apiName);
40
41    //复制 gradle文件到新的路径，作为该模块的gradle
42    copy() {
43        from "${apiGradle}/ApiBuildGralde.gradle"
44        into "${targetDir}/"
45    }
46
47    //删除空文件夹
48    deleteEmptyDir(new File(targetDir))
49
50    //重命名一下gradle
51    def build = new File(targetDir + "/ApiBuildGralde.gradle")
52    if (build.exists()) {
53        build.renameTo(new File(targetDir + "/build.gradle"))
54    }
55
56    // 重命名.api文件，生成正常的.java文件
57    renameApiFiles(targetDir, '.api', '.java')
58
59    //正常加载新的模块
60    include ":$sdkName"
61
62}


修改setting.gradle文件如下：



1//includeWithApi 在setting.gradle中定义
2include ':app', ':base'
3includeWithApi(":modulea",":base")
4includeWithApi(":moduleb",":base")


rebuild后，就可以看到moduleA-api,moduleB-api,并有对应的java文件如下图：







添加moduleA路由到moduleB，moduleB给moduleA发送事件逻辑，进行打包，会报如下错误：







很显然，ARouter注解处理器无法识别.api文件，path置为null处理，在moduleA,B添加对应的***-api模块依赖，就可以打包成功了。



奔着偷懒的原则，不想每次手动添加***-api模块依赖，自动动态添加依赖，实现gradle脚本如下：



 1ext{
 2   autoImportApiDependency = {extension -> //extension project对象
 3        def children = project.rootProject.childProjects
 4        //遍历所有child project
 5        children.each {child ->
 6            //判断 是否同时存在 *** module 和 ***-api module
 7            if(child.key.contains("-api") && children.containsKey(child.key.substring(0,child.key.length() - 4))){
 8                def targetKey = child.key.substring(0,child.key.length() - 4)
 9                def targetProject = children[targetKey]
10
11                targetProject.afterEvaluate {
12
13                    print '*********************\n'
14                    print targetProject.dependencies
15                    //通过打印 所有dependencies，推断需要添加如下两个依赖
16                    targetProject.dependencies.add("implementation",targetProject.dependencies.create(project(":" + child.key)))
17                    targetProject.dependencies.add("implementationDependenciesMetadata",targetProject.dependencies.create(project(":" + child.key)))
18                    print '*********************\n'
19
20                    //打印 module 添加的依赖
21                    targetProject.configurations.each {configuration ->
22                        print '\n---------------------------------------\n'
23                        configuration.allDependencies.each { dependency ->
24
25                            print configuration.name + "--->" +dependency.group + ":" + dependency.name + ":" + dependency.version +'\n'
26                        }
27
28                    }
29                }
30
31            }
32
33
34        }
35    } 
36}


autoImportApiDependency 方法封装在Config.gradle，在根build.gradle中调用



1apply from: 'Config.gradle'
2ext.autoImportApiDependency(this)


可以正常打包，并成功运行了。



遇坑集锦：



kotlin集成ARouter，尽管设置了AROUTER_MODULE_NAME，依然报如下错误：

ARouter::Compiler An exception is encountered, [null] 
可以考虑是否是gradle和 kotlin 版本的问题。



业务模块moduleA处于集成模式时，即集成到App壳工程中去，也会将单一模块做成App启动的源码和资源打包apk中，尽管设置了sourceSets,也没效果。







问题就出在debug文件夹的名字，把debug文件夹改成其他名字，就没有这个问题了，是不是很奇怪！没去究其原因。



参考资料：

微信 Android 模块化架构重构实践（上）
Android实现模块 api 化
美团猫眼电影Android模块化实战总结



