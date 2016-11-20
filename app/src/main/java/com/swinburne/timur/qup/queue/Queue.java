package com.swinburne.timur.qup.queue;

import android.os.Parcel;
import android.os.Parcelable;

public class Queue implements Parcelable {
    private Integer id;
    private String queueId;
    private String participantId;
    private String name;
    private String token;

    Queue(Integer id, String queueId, String participantId, String name, String token) {
        this.id = id;
        this.queueId = queueId;
        this.participantId = participantId;
        this.name = name;
        this.token = token;
    }

    protected Queue(Parcel in) {
        String[] data = new String[5];

        in.readStringArray(data);
        this.id = Integer.valueOf(data[0]);
        this.queueId = data[1];
        this.participantId = data[2];
        this.name = data[3];
        this.token = data[4];
    }

    public Integer getId() {
        return this.id;
    }

    public String getQueueId() {
        return this.queueId;
    }

    public String getParticipantId() {
        return this.participantId;
    }

    public String getName() {
        return this.name;
    }

    public String getToken() {
        return this.token;
    }

    /**
     * Default Parcel creator method, auto-generated stub
     */
    public static final Creator<Queue> CREATOR = new Creator<Queue>() {
        @Override
        public Queue createFromParcel(Parcel in) {
            return new Queue(in);
        }

        @Override
        public Queue[] newArray(int size) {
            return new Queue[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeStringArray(new String[] {
                this.id.toString(),
                this.queueId,
                this.participantId,
                this.name,
                this.token
        });
    }
}
