package com.buddynsoul.monitor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class PedometerFragment extends Fragment {
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    public PedometerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_pedometer, container, false);

        viewPager = (ViewPager) v.findViewById(R.id.viewpager_ID);
        adapter = new ViewPagerAdapter(getFragmentManager(), getActivity(), viewPager);
        viewPager.setAdapter(adapter);

        PedometerCurrentFragment current = new PedometerCurrentFragment();
        PedometerRecentFragment recent = new PedometerRecentFragment();

        adapter.addFrag(current, "current");
        adapter.addFrag(recent, "recent");
        adapter.notifyDataSetChanged();

//        viewPager.setCurrentItem(1);

        return v;
    }
}
