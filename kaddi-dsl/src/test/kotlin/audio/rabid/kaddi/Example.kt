package audio.rabid.kaddi

import audio.rabid.kaddi.dsl.*

object Example {

    class Logger {
        fun log(message: String) {
            // implementation
        }
    }

    class LoggerProvider : Provider<Logger> {
        override fun get(): Logger = Logger()
    }

    val LoggingModule = module("Logging") {
        bind<Logger>().with(LoggerProvider())
    }

    interface IDatabase
    class Database : IDatabase, ScopeClosable {

        var isClosed = false

        override fun onScopeClose() {
            isClosed = true
        }
    }

    val DataModule = module("Data") {
        require(LoggingModule)
        bind<IDatabase>().withSingleton { Database() }
    }

    class Application {
        fun onApplicationCreate() {
            Kaddi.createScope(this, AppModule)
        }
    }

    val AppModule = module("App") {
        require(LoggingModule)
        require(DataModule)
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

        bind<FragmentViewModel>().withSingleton {
            FragmentViewModel(logger = instance())
        }
    }

    class Activity2ViewModel(val logger: Logger, val database: IDatabase)

    val Activity2Module = module("Activity2") {

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