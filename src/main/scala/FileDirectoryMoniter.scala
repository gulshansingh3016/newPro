import java.nio.file.{Path, WatchEvent}

trait FileDirectoryMoniter {
  val root: Path                                  // starting file
  def start(): Unit                               // start the monitor
  def onCreate(path: Path) = {}                   // on-create callback
  def onModify(path: Path) = {}                   // on-modify callback
  def onDelete(path: Path) = {}                   // on-delete callback
  def onUnknownEvent(event: WatchEvent[_]) = {}   // handle lost/discarded events
  def onException(e: Throwable) = {}              // handle errors e.g. a read error
  def stop(): Unit                                // stop the monitor
}