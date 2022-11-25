/* SPDX-License-Identifier: Apache-2.0 */
/* Copyright Contributors to the ODPi Egeria project. */
package org.odpi.openmetadata.adapters.connectors.integration.lineage.utils;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
public class EventProducerUtility {

    private KafkaProducer producer;
    private int messageNoCount = 0;

    public static void main(String[] args) throws IOException {
        EventProducerUtility eventProducerUtility = new EventProducerUtility();
        eventProducerUtility.run();
    }

    private void run() throws IOException {
        producer = getKafkaProducer();


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
            Scanner sc = new Scanner(System.in); //System.in is a standard input stream

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
        String content = Files.readString(path);
        return content;
    }

    private void sendMessage(String messageStr) {
        try {
            producer.send(new ProducerRecord<>("legacyLineage",
                    messageNoCount++,
                    messageStr)).get();
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
            // handle the exception
        }
    }

    private KafkaProducer getKafkaProducer() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "localhost:9092");
        properties.put("client.id", "DemoProducer");
        properties.put("key.serializer", "org.apache.kafka.common.serialization.IntegerSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        KafkaProducer producer = new KafkaProducer<>(properties);
        return producer;
    }
}
