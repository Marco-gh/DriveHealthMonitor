package it.univaq.app.carapp.View;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import it.univaq.app.carapp.R;

public class detailFragmentSessions extends Fragment {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    /*private void setEntriesData() {
        try {
            ArrayList entries = new ArrayList();
            entries.clear();
            JSONArray jsonArray = new JSONArray("...."); // LEGGERE FILE JSON
            for (int i = 0; i < 10; i++) {
                JSONObject jo = jsonArray.getJSONObject(i);
                entries.add(new Entry(Float.parseFloat(jo.getString("valore")), i));
                labels.add(jo.getString("causale"));
            }
            PieDataSet dataset = new PieDataSet(entries, "");
            PieChart chart = (PieChart) getView().findViewById(R.id.chart);
            dataset.setColors(colors);
            PieData data = new PieData(labels, dataset);
            chart.setData(data);
        } catch (IOException ex) {
        } catch (JSONException ex) {
        }
    }*/

}
