package audio.rabid.kaddi

import audio.rabid.kaddi.dsl.*

object Example {

    interface LoggerEndpoint {
        fun handleLog(message: String)
    }

    class Logger(val endpoints: List<LoggerEndpoint>) {
        fun log(message: String) {
            for (endpoint in endpoints) {
                endpoint.handleLog(message)
            }
        }
    }

    val LoggingModule = module("Logging") {

        declareSetBinding<LoggerEndpoint>()

        bind<Logger>().with {
            Logger(endpoints = setInstance<LoggerEndpoint>().toList())
        }
    }

    interface IDatabase
    class Database : IDatabase, ScopeClosable {

        var isClosed = false

        override fun onScopeClose() {
            isClosed = true
        }
    }

    class DatabaseProvider : Provider<IDatabase> {
        override fun get(): IDatabase = Database()
    }

    class ALoggerEndpoint : LoggerEndpoint {
        override fun handleLog(message: String) {
            // handle
        }
    }

    val DataModule = module("Data") {
        import(LoggingModule)
        bind<IDatabase>().withSingleton(DatabaseProvider())

        bindIntoSet<LoggerEndpoint>().with { ALoggerEndpoint() }
    }

    class Application {
        fun onApplicationCreate() {
            Kaddi.createScope(this, AppModule)
        }
    }

    val AppModule = module("App") {
        import(LoggingModule)
        import(DataModule)
        bind<String>("AppName").toInstance("MyApp")
    }

    class Activity1(val application: Application) {
        val viewModel by inject<Activity1ViewModel>()
        val logger by inject<Logger>()

        fun onCreate() {
            Kaddi.getScope(application)
                    .createChildScope(this, Activity1Module)
                    .inject(this)
            logger.log("foo")
        }

        fun onDestroy() {
            Kaddi.closeScope(this)
        }
    }

    class Activity1ViewModel(val database: IDatabase, val appName: String)

    val Activity1Module = module("Activity1") {

        require<IDatabase>()
        require<String>("AppName")
        bind<Activity1ViewModel>().withSingleton {
            Activity1ViewModel(
                    database = instance(),
                    appName = instance("AppName")
            )
        }
    }

    class Fragment {
        val viewModel by inject<FragmentViewModel>()

        fun onAttach(activity: Any) {
            Kaddi.getScope(activity)
                    .createChildScope(this, FragmentModule)
                    .inject(this)
        }

        fun onDetach() {
            Kaddi.closeScope(this)
        }
    }

    class FragmentViewModel(val logger: Logger)

    val FragmentModule = module("Fragment") {

        require<Logger>()
        bind<FragmentViewModel>().withSingleton {
            FragmentViewModel(logger = instance())
        }
    }

    class Activity2ViewModel(val logger: Logger, val database: IDatabase)

    val Activity2Module = module("Activity2") {

        require<Logger>()
        require<IDatabase>()
        bind<Activity2ViewModel>().withSingleton {
            Activity2ViewModel(
                    logger = instance(),
                    database = instance()
            )
        }
    }

    class Activity2(val application: Application) {
        val viewModel by inject<Activity2ViewModel>()

        fun onCreate() {
            Kaddi.getScope(application)
                    .createChildScope(this, Activity2Module)
                    .inject(this)
        }

        fun onDestroy() {
            Kaddi.closeScope(this)
        }
    }
}
