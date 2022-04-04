package edu.byu.cs240.familymap;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import model.Event;
import model.Person;

public class PersonActivity extends AppCompatActivity {
    private final DataCache dataCache = DataCache.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_person);

        Person currPerson = dataCache.getClickedPerson();

        if (currPerson != null) {
            ExpandableListView expandableListView = findViewById(R.id.expandable_list_view);

            List<Event> events = dataCache.getEventsFor(currPerson.getPersonID());
            List<Person> familyMembers = dataCache.getFamilyFor(currPerson.getPersonID());

            expandableListView.setAdapter(new ExpandableListAdapter(events, familyMembers));

            // Setting name and gender
            TextView firstNameText = findViewById(R.id.person_first_name);
            TextView lastNameText = findViewById(R.id.person_last_name);
            TextView genderText = findViewById(R.id.person_gender);

            Person clickedPerson = dataCache.getClickedPerson();

            firstNameText.setText(clickedPerson.getFirstName());
            lastNameText.setText(clickedPerson.getLastName());
            if (clickedPerson.getGender().equals("m")) {
                genderText.setText(R.string.male);
            }else {
                genderText.setText(R.string.female);
            }
        }else {
            System.out.println("Current person is null, datacache clicked person is null");
        }

    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home)  {
            Intent intent = new Intent(this, MainActivity.class);
//            Don't create new activity and put on top of another, other one if main activity but buried in backstack, pop everything in backstack until get to it
            intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
        return true;
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {

        private static final int EVENTS_GROUP_POSITION = 0;
        private static final int FAMILY_GROUP_POSITION = 1;

        private final List<Event> events;
        private final List<Person> family;

        ExpandableListAdapter(List<Event> events, List<Person> family) {
            this.events = events;
            this.family = family;
        }

        @Override
        public int getGroupCount() {
            return 2;
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            switch (groupPosition) {
                case EVENTS_GROUP_POSITION:
                    return events.size();
                case FAMILY_GROUP_POSITION:
                    return family.size();
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }
        }

        @Override
        public Object getGroup(int groupPosition) {
            // Not used
            return null;
        }

        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // Not used
            return null;
        }

        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
            if(convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.person_event_group, parent, false);
            }

            TextView titleView = convertView.findViewById(R.id.listTitle);

            switch (groupPosition) {
                case EVENTS_GROUP_POSITION:
                    titleView.setText(R.string.eventsTitle);
                    break;
                case FAMILY_GROUP_POSITION:
                    titleView.setText(R.string.familyTitle);
                    break;
                default:
                    throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return convertView;
        }

        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
            View itemView;

            if (groupPosition == EVENTS_GROUP_POSITION || groupPosition == FAMILY_GROUP_POSITION) {
                itemView = getLayoutInflater().inflate(R.layout.person_event_item, parent, false);

                if (groupPosition == EVENTS_GROUP_POSITION) {
                    initializeEventView(itemView, childPosition);
                }else {
                    initializeFamilyView(itemView, childPosition);
                }
            }else {
                throw new IllegalArgumentException("Unrecognized group position: " + groupPosition);
            }

            return itemView;
        }

        private void initializeEventView(View eventItemView, final int childPosition) {
            TextView icon = eventItemView.findViewById(R.id.person_event_icon);
            icon.setText(R.string.fa_marker);

            TextView eventDetailView = eventItemView.findViewById(R.id.person_event_line_1);
            eventDetailView.setText(dataCache.eventToString(events.get(childPosition)));

            TextView personNameView = eventItemView.findViewById(R.id.person_event_line_2);
            personNameView.setText(dataCache.getFullName(dataCache.getClickedPerson()));

            eventItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"Clicked event view ",Toast.LENGTH_LONG).show();
                    dataCache.setClickedEvent(events.get(childPosition));
                    Intent switchActivityIntent = new Intent(getApplicationContext(), EventActivity.class);
                    startActivity(switchActivityIntent);
                }
            });
        }

        private void initializeFamilyView(View familyItemView, final int childPosition) {
            Person currFamilyMember = family.get(childPosition);

            TextView icon = familyItemView.findViewById(R.id.person_event_icon);

            if (currFamilyMember.getGender().equals("m")) {
                icon.setText(R.string.fa_male);
                icon.setTextColor(getResources().getColor(R.color.blue));
            }else {
                icon.setText(R.string.fa_female);
                icon.setTextColor(getResources().getColor(R.color.pink));
            }

            TextView familyMemberNameView = familyItemView.findViewById(R.id.person_event_line_1);
            familyMemberNameView.setText(dataCache.getFullName(currFamilyMember));

            String relationship = dataCache.getRelationshipBetween(dataCache.getClickedPerson(), currFamilyMember);
            TextView relationshipView = familyItemView.findViewById(R.id.person_event_line_2);
            relationshipView.setText(relationship);

            familyItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(),"Clicked person view ",Toast.LENGTH_LONG).show();
                    dataCache.setClickedPerson(family.get(childPosition));
                    Intent switchActivityIntent = new Intent(getApplicationContext(), PersonActivity.class);
                    startActivity(switchActivityIntent);
                }
            });
        }

        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return true;
        }
    }
}