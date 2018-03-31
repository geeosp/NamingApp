package com.naming.geeosp.naming;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.anychart.anychart.AnyChart;
import com.anychart.anychart.AnyChartView;
import com.anychart.anychart.CategoryValueDataEntry;
import com.anychart.anychart.DataEntry;
import com.anychart.anychart.OrdinalColor;
import com.anychart.anychart.TagCloud;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;


public class MainActivity extends AppCompatActivity {
    int synonimusWeight = 1;
    int relatedWeight = 1;
    int adjectivesWeight = 1;
    String DEBUG = "DEBUG";
    int maxDeep = 2;
    RequestQueue queue;
    AnyChartView anyChartView;
    EditText editText;
    HashMap<String, WordSet> wordHash;
    AtomicInteger requestedCounter;
    AtomicInteger receivedCounter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        queue = Volley.newRequestQueue(this);


        anyChartView = findViewById(R.id.any_chart_view);

        editText = findViewById(R.id.words);
        wordHash = new HashMap<>();
        //geo    updateChart();
    }

    public void updateChart(View v) {
        Log.e(DEBUG, "updating....");
        TagCloud tagCloud = AnyChart.tagCloud();

        tagCloud.setTitle("World Population");

        OrdinalColor ordinalColor = new OrdinalColor();
        ordinalColor.setColors(new String[]{
                "#26959f", "#f18126", "#3b8ad8", "#60727b", "#e24b26"
        });
        tagCloud.setColorScale(ordinalColor);
        tagCloud.setAngles(new Double[]{-90d, 0d, 90d});

        tagCloud.getColorRange().setEnabled(true);
        tagCloud.getColorRange().setColorLineSize(15d);

        List<DataEntry> data = new ArrayList<>();
        Iterator<WordSet> it = wordHash.values().iterator();
        while (it.hasNext()) {
            WordSet s = it.next();
            data.add(new CategoryValueDataEntry(s.x, s.category, s.value));
;
        }
        tagCloud.setData(data);
        anyChartView.setChart(tagCloud);
    }

    void requestWord(String myWord, final int currDeep, final String category) {
        Log.e(DEBUG, "requesting word: " + myWord);
        if (!wordHash.containsKey(myWord)) {
            requestedCounter.incrementAndGet();
            String url = " https://api.datamuse.com/words?ml=" + myWord;

            JsonArrayRequest jsonObjectRequest = new JsonArrayRequest(Request.Method.GET, url, null, new Response.Listener<JSONArray>() {

                @Override
                public void onResponse(JSONArray response) {
                    receivedCounter.incrementAndGet();
                    for (int i = 0; i < Math.min(10,response.length()); i++) {
                        Log.e("ResponseArray",
                                "ResponseArray: "+response.toString());
                        try {

                            JSONObject ob = response.getJSONObject(i);
                        Log.e("ResponseObject" ,
                                    "ResponseObject: "+ob.toString());
                            if (currDeep < maxDeep&&!wordHash.containsKey(ob.getString("word"))) {
                                requestWord(ob.getString("word"), currDeep + 1, category);

                            } else {

                            }
                            addWord(ob.getString("word"), ob.getInt("score"), category);
                        } catch (Exception e) {
                          Log.e("JSONException", e.getMessage());
                        }
                    }
                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                Log.e("ResponseError", error.toString());
                }
            });


            queue.add(jsonObjectRequest);
        }
    }

    class WordSet {
        public String x = "", category = "";
        public int value = 0;

    }

    void addWord(String word, long value, String category) {

        WordSet set;
        if (wordHash.containsKey(word)) {
            set = wordHash.get(word);
            wordHash.remove(word);
        } else {
            set = new WordSet();
            set.x = word;
            set.category = category;

        }
        set.value += value;
        wordHash.put(word, set);
    }

    public void onGo(View v) {

        String[] words = editText.getText().toString().split(" ");
        wordHash = new HashMap<>();
        requestedCounter = new AtomicInteger(0);
        receivedCounter = new AtomicInteger(0);
        for (int i = 0; i < words.length; i++) {
      //      addWord(words[i], 10000);

            requestWord(words[i], 0, words[i]);
        }
    }
}