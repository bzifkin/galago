package org.lemurproject.galago.core.tools.apps;

import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.utility.Parameters;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.*;

/**
 * Created by bzifkin on 6/3/16.
 */


    public class CommandLineTest {

    public static String readString(String prompt) {
        System.out.print(prompt);
        StringBuilder sb = new StringBuilder();
        while (true) {
            int ch = 0;
            try {
                ch = System.in.read();
            } catch (IOException e) {
                break;
            }
            if (ch == -1) return null;
            if (ch == '\n' || ch == '\r') break;
            sb.append((char) ch);
        }
        return sb.toString();
    }

    public static void main(String[] args) throws Exception {

        String indexPath = "/home/bzifkin/IdeaProjects/lemur-galago/carribean-books";
        String id = "sirwilliampennhi01hami";
        Document.DocumentComponents dc = new Document.DocumentComponents(true, false, true);
        Retrieval r = RetrievalFactory.instance(indexPath, Parameters.create());
        Document document = r.getDocument(id, dc);

            HashMap<String, Integer> locations = new HashMap<>();

                for (Tag tag : document.tags) {

                    if (!tag.name.equals("location")) continue;
                    List<String> subList = document.terms.subList(tag.begin, tag.end);
                    String field = Utility.join(subList, " ");
                    locations.put(field, tag.begin);
                }
        StoreReader reader = PalDB.createReader(new File("/home/bzifkin/IdeaProjects/lemur-galago/cities.paldb"));

        Iterator it = locations.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            String k = (String) entry.getKey();
            Integer v = (Integer)entry.getValue();
            HashSet<String> asked = new HashSet<>();

            System.out.println("Retrieved this location: " + k);
            if (reader.getArray(k) == null){
              System.out.println("But it wasn't in our database :(");
                continue;
            }

            if (asked.add(k) == false)continue;
                String[] temp = k.split("\\s+");
                List<String> window = document.terms.subList(v - 15, v + 15 +temp.length);
                String context = Utility.join(window, " ");
                System.out.println("Context For Location("+ k+") : " + context);
                System.out.println("These are our candidates from our database");
                String[] candidates = reader.getArray(k);
                //if(candidates.length <=3)
                int limit = (candidates.length < 10) ? candidates.length : 10;
                for (int i = 0; i <= limit - 1; i++) {
                    System.out.print((i + 1) + ") " + candidates[i]);
                }

            String user = readString("Which one is right? ");

            if (user == null || user.equals("q")) break;
            if (user.isEmpty()) {
                System.out.println("skip");
            }
            try {
                int number = Integer.parseInt(user);
                System.out.println("Great! you identified the location as #" + number);
            } catch (NumberFormatException nfe) {
                System.out.println("Not a Number!");
                continue;
            }
        }
        reader.close();
        // keep going forever.

    }
}


