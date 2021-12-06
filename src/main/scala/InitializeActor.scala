import FileWatcher.{Callback, Event, Message}

import java.nio.file.StandardWatchEventKinds._
import akka.actor.{ActorRef, ActorSystem, Props}

import java.nio.file._

object InitializeActor extends App{
  // initialize the actor instance
  val system = ActorSystem("mySystem")
  val watcher: ActorRef = system.actorOf(Props(new ActorFileWatcher(Paths.get("/home/knoldus/Projectknuron1"))))

  // util to create a RegisterCallback message for the actor
  def when(events: Event*)(callback: Callback): Message = {
    Message.RegisterCallback(events.distinct, callback)
  }

  // send the register callback message for create/modify events
  watcher ! when(events = ENTRY_CREATE, ENTRY_MODIFY) {
    case (ENTRY_CREATE, file) => println(s"$file got created")
    case (ENTRY_MODIFY, file) => println(s"$file got modified")
  }

}
