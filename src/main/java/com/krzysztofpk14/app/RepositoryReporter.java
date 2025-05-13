package com.krzysztofpk14.app;

import java.util.*;
import java.io.InputStream;
import java.io.PrintStream;
// import javax.xml.bind.JAXBContext;
// import javax.xml.bind.JAXBException;
// import javax.xml.bind.Unmarshaller;
import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Unmarshaller;

import io.fixprotocol._2016.fixrepository.ComponentRefType;
import io.fixprotocol._2016.fixrepository.FieldRefType;
import io.fixprotocol._2016.fixrepository.GroupRefType;
import io.fixprotocol._2016.fixrepository.MessageType;
import io.fixprotocol._2016.fixrepository.Repository;
// import scala.collection.immutable.List;

public class RepositoryReporter {
  
  
  public void report(InputStream is, PrintStream os) {
    try {
      // parse the XML
      Repository repository = unmarshal(is);
      // report messages
      List<MessageType> messageList = repository.getMessages().getMessage();
      reportMessages(messageList, os);
    } catch (JAXBException e) {
      // print exception
      os.format("ERROR: %s%n", e.getMessage());
    }
  }

  void reportMessages(List<MessageType> messageList, PrintStream os) {
    // iterate all messages in the file
    for (MessageType message: messageList) {
      os.format("Message name: %s scenario: %s MsgType: %s%n", 
          message.getName(), message.getScenario(), message.getMsgType());
      // report message members
      List<Object> members = message.getStructure().getComponentRefOrGroupRefOrFieldRef();
      reportMembers(members, os);
    }  
  }

  void reportMembers(List<Object> members, PrintStream os) {
    for (Object member : members) {
      if (member instanceof FieldRefType) {
        FieldRefType fieldRef = (FieldRefType)member;
        os.format("\tFieldRef id: %d scenario: %s%n", fieldRef.getId(), fieldRef.getScenario());
      } else if (member instanceof ComponentRefType) {
        ComponentRefType componentRef = (ComponentRefType)member;
        os.format("\tComponentRef id: %d scenario: %s%n", componentRef.getId(), componentRef.getScenario());
      } else if (member instanceof GroupRefType) {
        GroupRefType groupRef = (GroupRefType)member;
        os.format("\tGroupRef id: %d scenario: %s%n", groupRef.getId(), groupRef.getScenario());
      }
    }
  }
  
  private Repository unmarshal(InputStream is) throws JAXBException {
    final JAXBContext jaxbContext = JAXBContext.newInstance(Repository.class);
    final Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
    return (Repository) jaxbUnmarshaller.unmarshal(is);
  }

}
