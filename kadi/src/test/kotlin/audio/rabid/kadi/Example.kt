package audio.rabid.kadi

object Example {

    class Logger {
        fun log(message: String) {
            println(message)
        }
    }

    class LoggerProvider : Provider<Logger> {
        override fun get(): Logger = Logger()
    }

    val LoggingModule = module("Logging") {
        bind<Logger>().with(LoggerProvider())
    }

    object LoggingModule2 : Module("Logging", allowOverrides = false) {
        init {
            addBinding(Binding(BindingKey(Logger::class), false, LoggerProvider()))
        }
    }

    interface IDatabase
    class Database: IDatabase, ScopeClosable {

        var isClosed = false

        override fun onScopeClose() {
            isClosed = true
        }
    }

    val DataModule = module("Data") {
        require(LoggingModule)
        bind<IDatabase>().withSingleton { Database() }
    }

    object DataModule2 :  Module("Data", allowOverrides = false) {
        init {
            require(LoggingModule2)
            addBinding(Binding(BindingKey(IDatabase::class), false, SingletonProvider<IDatabase>(LambdaProvider2 { Database() })))
        }
    }

    class Application {
        fun onApplicationCreate() {
//            Kadi.createChildScope(this, AppModule)
            Kadi.createChildScope(this, AppModule2())
        }
    }

    val AppModule = module("App") {
        require(LoggingModule)
        require(DataModule)
        bind<String>("AppName").toInstance("MyApp")
    }

    class AppModule2 : Module("App", allowOverrides = false) {
        init {
            require(LoggingModule2)
            require(DataModule2)
            addBinding(Binding(BindingKey(String::class, "AppName"), false, JustProvider("MyApp")))
        }
    }

    class Activity1(val application: Application) {
        val viewModel by inject<Activity1ViewModel>()
        val logger by inject<Logger>()

        fun onCreate() {
            Kadi.getScope(application)
//                .createChildScope(this, Activity1Module)
                .createChildScope(this, Activity1Module2())
                .inject(this)
            logger.log("foo")
        }

        fun onDestroy() {
            Kadi.closeScope(this)
        }
    }

    class Activity1ViewModel(val database: IDatabase, val appName: String)

    val Activity1Module = module("Activity1") {

        bind<Activity1ViewModel>().withSingleton {
            Activity1ViewModel(
                database = get(),
                appName = get("AppName")
            )
        }
    }

    class Activity1Module2 :  Module("Activity1", allowOverrides = false) {
        init {
            require(AppModule2())
            addBinding(Binding(BindingKey(Activity1ViewModel::class), false, SingletonProvider<Activity1ViewModel>(LambdaProvider2(this::viewModel))))
        }

        fun viewModel(): Activity1ViewModel {
            return viewModel(scope.get<IDatabase>(), scope.get<String>("AppName"))
        }

        fun viewModel(datatabase: IDatabase, @Named("AppName") appName: String) : Activity1ViewModel {
            return Activity1ViewModel(datatabase, appName)
        }
    }

    class Fragment {
        val viewModel by inject<FragmentViewModel>()

        fun onAttach(activity: Any) {
            Kadi.getScope(activity)
//                .createChildScope(this, FragmentModule)
                .createChildScope(this, FragmentModule2())
                .inject(this)
        }

        fun onDetach() {
            Kadi.closeScope(this)
        }
    }

    class FragmentViewModel(val logger: Logger)

    val FragmentModule = module("Fragment") {

        bind<FragmentViewModel>().withSingleton {
            FragmentViewModel(logger = get())
        }
    }

    class FragmentModule2 :  Module("Fragment", allowOverrides = false) {
        init {
            require(AppModule2())
            require(Activity1Module2())
            addBinding(Binding(BindingKey(FragmentViewModel::class), false, SingletonProvider<FragmentViewModel>(LambdaProvider2(this::viewModel))))
        }

        fun viewModel(): FragmentViewModel {
            return viewModel(scope.get<Logger>())
        }

        fun viewModel(logger: Logger) : FragmentViewModel {
            return FragmentViewModel(logger)
        }
    }

    class Activity2ViewModel(val logger: Logger, val database: IDatabase)

    val Activity2Module = module("Activity2") {

        bind<Activity2ViewModel>().withSingleton {
            Activity2ViewModel(
                logger = get(),
                database = get()
            )
        }
    }

    class Activity2Module2 :  Module("Activity2", allowOverrides = false) {
        init {
            require(AppModule2())
            addBinding(Binding(BindingKey(Activity2ViewModel::class), false, SingletonProvider<Activity2ViewModel>(LambdaProvider2(this::viewModel))))
        }

        fun viewModel(): Activity2ViewModel {
            return viewModel(scope.get<Logger>(), scope.get<IDatabase>())
        }

        fun viewModel(logger: Logger, database: IDatabase) : Activity2ViewModel {
            return Activity2ViewModel(logger, database)
        }
    }

    class Activity2(val application: Application) {
        val viewModel by inject<Activity2ViewModel>()

        fun onCreate() {
            Kadi.getScope(application)
//                .createChildScope(this, Activity2Module)
                .createChildScope(this, Activity2Module2())
                .inject(this)
        }

        fun onDestroy() {
            Kadi.closeScope(this)
        }
    }
}
