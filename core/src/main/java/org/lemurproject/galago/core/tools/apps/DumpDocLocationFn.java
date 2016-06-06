package org.lemurproject.galago.core.tools.apps;


import java.io.*;


import com.linkedin.paldb.api.PalDB;
import com.linkedin.paldb.api.StoreReader;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Document.DocumentComponents;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.core.retrieval.Retrieval;
import org.lemurproject.galago.core.retrieval.RetrievalFactory;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.utility.tools.AppFunction;
import org.lemurproject.galago.utility.Parameters;
import java.util.*;
/**
 * Created by bzifkin on 2/19/16.
 * I added it to Mercurial, maybe didn't need to
 * TODO: ask about ^
 */
public class DumpDocLocationFn extends AppFunction {

    @Override
    public String getName() {
        return "dump-doc-location";
    }

    @Override
    public String getHelpString() {
        return "galago dump-doc-location --index=<index_part> --iidList=<list_of_iids> --eidList=<list_of_eids>\n\n"
                + "  Dumps the location statistics from various inverted list data for one or more specified\n"
                + "  documents.  Location statistics are output in CSV format.\n\n"
                + "  Internal and External document IDs are specified using the appropriate command line flag.\n"
                + "  One must use a trailing comma if only one IID is specified so the processor knows it is a string\n"
                + "  and not a number.  If spaces are placed between listed IDs, the entire ID string should be quoted.\n\n";
    }
//    public Document getDocument(String identifier, DocumentComponents p) throws IOException {
//        return retrieval.getDocument(identifier, p);
//    }

    @Override
    public void run(Parameters p, PrintStream output) throws Exception {

        //StoreReader reader = PalDB.createReader(new File("p_names.paldb"));


        if (p.get("help", false)) {
            output.println(getHelpString());
            return;
        }
        String indexPath = p.getAsString("index");
        String identifier = p.getAsString("id");
        HashMap<String, ArrayList<Integer>> locations = new HashMap<String, ArrayList<Integer>>();
        DocumentComponents dc = new DocumentComponents(p); //this is the part that checks if you want metadata, text, tags, etc
        Retrieval r = RetrievalFactory.instance(indexPath, Parameters.create());
        assert r.getAvailableParts().containsKey("corpus") : "Index does not contain a corpus part.";
        Document document = r.getDocument(identifier, dc);
        if (document != null) {
            for (Tag tag : document.tags) {

                  if(!tag.name.equals("location")) continue;
                //document.text.substring(tag.charBegin, tag.charEnd);
                List<String> subList = document.terms.subList(tag.begin, tag.end);
                System.out.println("Sublist: " + subList.toString());

                String field = Utility.join(subList, " ");
                System.out.println("Field: "+ field);
                if (locations.get(field) == null) {
                    ArrayList<Integer> indicies = new ArrayList<Integer>();
                    indicies.add(tag.begin);
                    locations.put(field, indicies); //no ArrayList assigned, create new ArrayList
                }
                else
                    locations.get(field).add(tag.begin);

            }
            //output.println(document.toString());
        } else {
            output.println("Document " + identifier + " does not exist in index " + indexPath + ".");
        }
        StringBuilder sb = new StringBuilder();
        for(Map.Entry<String, ArrayList<Integer>> location : locations.entrySet())
        {
            //System.out.println(location.getKey() + "\t" + location.getValue());
            String loc = location.getKey();
//            if(reader.contains(location.getKey())==false){
//                sb.append(loc+ " could not be found in p_names\n");
//            }
             sb.append(loc + location.getValue()+ "\n");

        }
        System.out.println(sb.toString());
    }
}
