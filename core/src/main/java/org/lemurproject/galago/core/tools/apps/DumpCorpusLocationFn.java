package org.lemurproject.galago.core.tools.apps;

import java.io.*;

import org.lemurproject.galago.core.index.corpus.CorpusReader;
import org.lemurproject.galago.core.index.corpus.DocumentReader;
import org.lemurproject.galago.core.parse.Document;
import org.lemurproject.galago.core.parse.Document.DocumentComponents;
import org.lemurproject.galago.core.parse.Tag;
import org.lemurproject.galago.tupleflow.Utility;
import org.lemurproject.galago.utility.Parameters;
import org.lemurproject.galago.utility.tools.AppFunction;

import java.util.*;

/**
 * Created by bzifkin on 6/3/16.
 */
public class DumpCorpusLocationFn extends AppFunction {
    public String getName() {
        return "dump-corpus-location";
    }

    @Override
    public String getHelpString() {
        return "galago dump-corpus-location --path=<path_part> [limit fields]\n\n"
                + "  Dumps the location statistics from various inverted list data for a corpus\n"
                + "  Location statistics are output in CSV format.\n\n"
                + "  Internal and External document IDs are specified using the appropriate command line flag.\n"
                + "  One must use a trailing comma if only one IID is specified so the processor knows it is a string\n"
                + "  and not a number.  If spaces are placed between listed IDs, the entire ID string should be quoted.\n\n";
    }


    public void writeLocationToFile(String name, HashMap<String, ArrayList<Integer>> locations) throws IOException {
        try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter
                (new FileOutputStream("/home/bzifkin/IdeaProjects/lemur-galago/location-files/" + name + "-locations.txt/")))){


            for (Map.Entry<String, ArrayList<Integer>> location : locations.entrySet()) {
                String loc = location.getKey();
                bw.write(loc + location.getValue() + "\n");

            }
            bw.flush();
            bw.close();
        }

    }

    @Override
    public void run(Parameters p, PrintStream output) throws Exception {

        //StoreReader reader = PalDB.createReader(new File("p_names.paldb"));


        if (p.get("help", false)) {
            output.println(getHelpString());
            return;
        }


        CorpusReader reader = new CorpusReader(p.getString("path"));
        if (reader.getManifest().get("emptyIndexFile", false)) {
            output.println("Empty Corpus.");
            return;
        }
        DocumentReader.DocumentIterator iterator = reader.getIterator();
        DocumentComponents dc = new DocumentComponents(p);

        while (!iterator.isDone()) {
            HashMap<String, ArrayList<Integer>> locations = new HashMap<String, ArrayList<Integer>>();

            Document document = iterator.getDocument(dc);
            output.println("#NAME: " + document.name);
            if (document != null) {
                for (Tag tag : document.tags) {

                    if (!tag.name.equals("location")) continue;
                    document.text.substring(tag.charBegin, tag.charEnd);
                    List<String> subList = document.terms.subList(tag.begin, tag.end);
                    String field = Utility.join(subList, " ");
                    if (locations.get(field) == null) {
                        ArrayList<Integer> indicies = new ArrayList<Integer>();
                        indicies.add(tag.begin);
                        locations.put(field, indicies); //no ArrayList assigned, create new ArrayList
                    } else
                        locations.get(field).add(tag.begin);

                }
                writeLocationToFile(document.name, locations);
            }
            iterator.nextKey();
        }
        reader.close();

    }
}
