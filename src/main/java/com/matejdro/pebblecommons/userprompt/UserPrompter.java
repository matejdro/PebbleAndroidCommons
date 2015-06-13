package com.matejdro.pebblecommons.userprompt;

import android.content.Intent;
import android.support.annotation.Nullable;

public interface UserPrompter
{
    void promptUser(String title, @Nullable String subtitle, String body, PromptAnswer... answers);

    class PromptAnswer
    {
        private String text;
        private Intent action;

        public PromptAnswer(String text, Intent action)
        {
            this.text = text;
            this.action = action;
        }

        public String getText()
        {
            return text;
        }

        public Intent getAction()
        {
            return action;
        }
    }
}


