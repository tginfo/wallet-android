
package ru.olaf.custom.Api.Pojo.getTransactions;

import com.google.gson.annotations.SerializedName;

@SuppressWarnings("unused")
public class MsgData {

    @SerializedName("body")
    private String mBody;
    @SerializedName("text")
    private String mText;
    @SerializedName("@type")
    private String mType;

    public String getBody() {
        return mBody;
    }

    public void setBody(String body) {
        mBody = body;
    }

    public String getText() {
        return mText;
    }

    public void setText(String text) {
        mText = text;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        mType = type;
    }

}
