import FileWatcher.{Callback, Event}
import akka.actor._
import akka.io.Tcp.Message
import FileWatcher.{Callback, Event}

import java.nio.file._
import java.io.File
import java.nio.file.{Path, Paths, WatchEvent}
import scala.collection.mutable

class ActorFileWatcher(file: Path) extends ThreadFileWatch(file) with Actor {
  import FileWatcher._

  // MultiMap from Events to registered callbacks
  protected[this] val callbacks = newMultiMap[Event, Callback]

  // Override the dispatcher from ThreadFileMonitor to inform the actor of a new event
  override def dispatch(event: Event, file: Path) = self ! Message.NewEvent(event, file)

  // Override the onException from the ThreadFileMonitor
  override def onException(exception: Throwable) = self ! Status.Failure(exception)

  // when actor starts, start the ThreadFileMonitor
  override def preStart() = super.start()

  // before actor stops, stop the ThreadFileMonitor
  override def postStop() = super.interrupt()

  override def receive: Receive = {
    case Message.NewEvent(event, target) if callbacks contains event =>
      callbacks(event) foreach {f => f(event -> target)}

    case Message.RegisterCallback(events, callback) =>
      events foreach {event => callbacks.addBinding(event, callback)}

    case Message.RemoveCallback(event, callback) =>
      callbacks.removeBinding(event, callback)
  }
}

object FileWatcher {
  type Event = WatchEvent.Kind[Path]
  type Callback = PartialFunction[(Event, Path), Unit]

  sealed trait Message
  object Message {
    case class NewEvent(event: Event, file: Path) extends Message
    case class RegisterCallback(events: Seq[Event], callback: Callback) extends Message
    case class RemoveCallback(event: Event, callback: Callback) extends Message
  }
}

