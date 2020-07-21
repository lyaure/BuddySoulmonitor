package com.buddynsoul.monitor.Fragments.Admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.buddynsoul.monitor.Adapters.ViewPagerAdapter;
import com.buddynsoul.monitor.R;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;


public class UsersFragment extends Fragment {
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    public UsersFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_users, container, false);

        // multiple fragments in the same fragments
        viewPager = (ViewPager) v.findViewById(R.id.users_viewpager_ID);
        adapter = new ViewPagerAdapter(getFragmentManager(), getActivity(), viewPager);
        viewPager.setAdapter(adapter);

        // indicates number of fragments
        DotsIndicator dotsIndicator = (DotsIndicator)v.findViewById(R.id.dots_indicator);
        dotsIndicator.setViewPager(viewPager);

        UsersListFragment list = new UsersListFragment();
        UsersSearchFragment search = new UsersSearchFragment();

        // add fragments
        adapter.addFrag(list, "list");
        adapter.addFrag(search, "search");

        adapter.notifyDataSetChanged();


        return v;
    }
}
