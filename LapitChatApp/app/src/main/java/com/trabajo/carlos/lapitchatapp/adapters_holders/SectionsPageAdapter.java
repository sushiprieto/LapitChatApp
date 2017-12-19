package com.trabajo.carlos.lapitchatapp.adapters_holders;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.trabajo.carlos.lapitchatapp.fragments.ChatsFragment;
import com.trabajo.carlos.lapitchatapp.fragments.FriendsFragment;
import com.trabajo.carlos.lapitchatapp.fragments.RequestsFragment;

/**
 * Created by Carlos Prieto on 19/08/2017.
 */

public class SectionsPageAdapter extends FragmentPagerAdapter{

    public SectionsPageAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;

            case 1:
                ChatsFragment chatsFragment = new ChatsFragment();
                return chatsFragment;

            case 2:
                FriendsFragment friendsFragment = new FriendsFragment();
                return friendsFragment;

            default:
                return null;
        }

    }

    /**
     * Devolvemos 3 porque tenemos 3 paginas
     * @return
     */
    @Override
    public int getCount() {
        return 3;
    }

    public CharSequence getPageTitle(int position){

        switch (position){
            case 0:
                return "REQUEST";

            case 1:
                return "CHATS";

            case 2:
                return "FRIENDS";

            default:
                return null;
        }

    }

}
