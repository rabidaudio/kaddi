//package audio.rabid.kaddi
//
//object Example {
//    class Logger {
//        fun log(message: String) {
//            println(message)
//        }
//    }
//
//    class LoggerProvider : Provider<Logger> {
//        override fun get(): Logger = Logger()
//    }
//
//    class LoggingModule : Module("Logging", allowOverrides = false) {
//        init {
//            addBinding(Binding(BindingKey(Logger::class), false, LoggerProvider()))
//        }
//    }
//
//    interface IDatabase
//    class Database: IDatabase, ScopeClosable {
//
//        var isClosed = false
//
//        override fun onScopeClose() {
//            isClosed = true
//        }
//    }
//
//    class DataModule : Module("Data", allowOverrides = false) {
//        init {
//            require(LoggingModule())
//            addBinding(Binding(BindingKey(IDatabase::class), false, SingletonProvider<IDatabase>(LambdaProvider2 { Database() })))
//        }
//    }
//
//    class Application {
//        fun onApplicationCreate() {
//            Kaddi.createChildScope(this, AppModule())
//        }
//    }
//
//    class AppModule : Module("App", allowOverrides = false) {
//        init {
//            require(LoggingModule())
//            require(DataModule())
//            addBinding(Binding(BindingKey(String::class, "AppName"), false, JustProvider("MyApp")))
//        }
//    }
//
//    class Activity1(val application: Application) {
//        val viewModel by inject<Activity1ViewModel>()
//        val logger by inject<Logger>()
//
//        fun onCreate() {
//            Kaddi.getScope(application)
//                .createChildScope(this, Activity1Module2())
//                    .inject(this)
//            logger.log("foo")
//        }
//
//        fun onDestroy() {
//            Kaddi.closeScope(this)
//        }
//    }
//
//    class Activity1ViewModel(val database: IDatabase, val appName: String)
//
//    class Activity1Module2:  Module("Activity1", allowOverrides = false) {
//        init {
//            require(AppModule())
//            addBinding(Binding(BindingKey(Activity1ViewModel::class), false, SingletonProvider<Activity1ViewModel>(LambdaProvider2(this::viewModel))))
//        }
//
//        fun viewModel(): Activity1ViewModel {
//            return viewModel(scope.get<IDatabase>(), scope.get<String>("AppName"))
//        }
//
//        fun viewModel(datatabase: IDatabase, @Named("AppName") appName: String) : Activity1ViewModel {
//            return Activity1ViewModel(datatabase, appName)
//        }
//    }
//
//    class Fragment {
//        val viewModel by inject<FragmentViewModel>()
//
//        fun onAttach(activity: Any) {
//            Kaddi.getScope(activity)
//                .createChildScope(this, FragmentModule())
//                    .inject(this)
//        }
//
//        fun onDetach() {
//            Kaddi.closeScope(this)
//        }
//    }
//
//    class FragmentViewModel(val logger: Logger)
//
//    class FragmentModule :  Module("Fragment", allowOverrides = false) {
//        init {
//            require(AppModule())
//            require(Activity1Module2())
//            addBinding(Binding(BindingKey(FragmentViewModel::class), false, SingletonProvider<FragmentViewModel>(LambdaProvider2(this::viewModel))))
//        }
//
//        fun viewModel(): FragmentViewModel {
//            return viewModel(scope.get<Logger>())
//        }
//
//        fun viewModel(logger: Logger) : FragmentViewModel {
//            return FragmentViewModel(logger)
//        }
//    }
//
//    class Activity2ViewModel(val logger: Logger, val database: IDatabase)
//
//    class Activity2Module :  Module("Activity2", allowOverrides = false) {
//        init {
//            require(AppModule())
//            addBinding(Binding(BindingKey(Activity2ViewModel::class), false, SingletonProvider<Activity2ViewModel>(LambdaProvider2(this::viewModel))))
//        }
//
//        fun viewModel(): Activity2ViewModel {
//            return viewModel(scope.get<Logger>(), scope.get<IDatabase>())
//        }
//
//        fun viewModel(logger: Logger, database: IDatabase) : Activity2ViewModel {
//            return Activity2ViewModel(logger, database)
//        }
//    }
//
//    class Activity2(val application: Application) {
//        val viewModel by inject<Activity2ViewModel>()
//
//        fun onCreate() {
//            Kaddi.getScope(application)
//                .createChildScope(this, Activity2Module())
//                    .inject(this)
//        }
//
//        fun onDestroy() {
//            Kaddi.closeScope(this)
//        }
//    }
//}
