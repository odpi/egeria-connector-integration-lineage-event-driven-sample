/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.sample.utils;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

/**
 * This is a JAVA application to help to drive the connector for development testing purposes. It publishes events
 * to the local Kafka on the specified topic.
 *
 * Prior to running this application, you should have:
 * - a running Egeria OMAG platform with a metadata server and the lineage connector jar file present.
 * - the metadata server and lineage connector should be configured
 * - the topic referred to in the lineage connector configuration should have been created on the appropriate kafka.
 *
 * Run this application, you will be asked for topic name to write to, press enter if you want to use "legacyLineage"
 * as the topic name.
 * A list of files containing event payloads will be shown. Running them in order (sample1, sample2 etc...) will drive
 * the connector to issue some creations, updates and deletes.
 * Then copy and paste the json file you want to sent as an event to the topic. The application will continue to offer
 * inputs, in which you can continue to paste file names. Enter 'q' to quit.
 *
 * Testing of thi app, have been performed under Intellij IDE. Changes will be required if you wnt to run this application
 * from a jar file.
 *
 */
public class EventProducerUtility {

    public static  String topicName = "legacyLineage";
    private KafkaProducer<Integer,String> producer;
    private int messageNoCount = 0;

    public static void main(String[] args) throws IOException {
        EventProducerUtility eventProducerUtility = new EventProducerUtility();
        eventProducerUtility.run();
    }

    private void run() throws IOException {
        producer = getKafkaProducer();
        System.out.print("Input topic Name (default legacyLineage):\n ");
        Scanner sc = new Scanner(System.in); //System.in is a standard input stream

        String topicInput = sc.nextLine();
        if ( topicInput !=null && topicInput.length() >0 ) {
            topicName =  topicInput;
        }

        String folderPathStr =  "src/test/resources/";
        File[] files = new File(folderPathStr).listFiles();

        boolean processing = true;
        while (processing) {
            System.out.print("");
            System.out.print("Here are the valid file names to send:\n ");
            Set<String> fileNamesSet = new HashSet<>();
            for (File file : files) {
                System.out.println(file.getName());
                fileNamesSet.add(file.getName());
            }
            System.out.print("Copy and Paste the file name you want to send as an event or q to quit: ");

            String str = sc.nextLine();              //reads string
            if (fileNamesSet.contains(str)) {
                String textPath = folderPathStr + str;
                String fileContent = getFileContent(textPath);
                System.out.println("Message content is");
                System.out.println(fileContent);
                sendMessage(fileContent);
            } else if (str.equals("q")) {
                // quit loop
                processing = false;
            } else {
                System.out.print("You have entered: " + str + ". It is not in the list - try again. \n");
            }
        }

    }


    private String getFileContent(String textPath) throws IOException {
        Path path = Paths.get(textPath);
        return Files.readString(path);
    }

    private void sendMessage(String messageStr) {
        try {
            producer.send(new ProducerRecord<Integer,String>(topicName,
                    messageNoCount++,
                    messageStr)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private KafkaProducer<Integer,String> getKafkaProducer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("client.id", "DemoProducer");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        return new KafkaProducer<>(properties);
    }
}
