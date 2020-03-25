package com.tsvietok.meteoradar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class BottomNavigationDrawerFragment extends BottomSheetDialogFragment {
    private MapInfoListAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.AppTheme);
        return inflater.inflate(R.layout.bottom_nav, container, false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Toast.makeText(this.getContext(), item.getTitle(), Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        ListView mapInfoList = view.findViewById(R.id.mapInfoList);
        mapInfoList.setAdapter(adapter);
    }

    void setAdapter(MapInfoListAdapter adapter) {
        this.adapter = adapter;
    }
}
