import java.nio.file.StandardWatchEventKinds._
import java.nio.file._
import scala.collection.convert.ImplicitConversions.`iterator asScala`
//import scala.collection.convert.ImplicitConversions.`iterator asScala`
import scala.jdk.CollectionConverters._


class ThreadFileWatch(val root: Path) extends Thread with FileDirectoryMoniter {
  setDaemon(true) // daemonize this thread
  setUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler {
    override def uncaughtException(thread: Thread, exception: Throwable) = onException(exception)
  })

  val service = root.getFileSystem.newWatchService()

  override def run() = Iterator.continually(service.take()).foreach(process)

  override def interrupt() = {
    service.close()
    super.interrupt()
  }

  override def start() = {
    if (Files.isDirectory(root)) {
      watch(root, recursive = true)
    } else {
      watch(root.getParent, recursive = false)
    }
    super.start()
  }

  protected[this] def watch(file: Path, recursive: Boolean = true): Unit = {
    if (Files.isDirectory(file)) {
      file.register(service, ENTRY_CREATE, ENTRY_DELETE, ENTRY_MODIFY)
      if (recursive) {
        Files.list(file).iterator() foreach {f => watch(f, recursive)}
      }
    }
  }

  private[this] def reactTo(target: Path) = Files.isDirectory(root) || (root == target)

  protected[this] def process(key: WatchKey) = {
    key.pollEvents() forEach {
      case event: WatchEvent[Path] =>
        val target = event.context()
        if (reactTo(target)) {
          if (Files.isDirectory(root) && event.kind() == ENTRY_CREATE) {
            watch(root.resolve(target))
          }
          dispatch(event.kind(), target)
        }
      case event => onUnknownEvent(event)
    }
    key.reset()
  }

  def dispatch(eventType: WatchEvent.Kind[Path], file: Path): Unit = {
    eventType match {
      case ENTRY_CREATE => onCreate(file)
      case ENTRY_MODIFY => onModify(file)
      case ENTRY_DELETE => onDelete(file)
    }
  }
}