package com.ccsidd.rtone.objects.sip;

/**
 * Created by dung on 1/25/16.
 */
public enum ConflictState {
    None("None"), WarningShown("WarningShown"), WarningNoShown("WarningNoShown");
    private String state;
    ConflictState(String state)
    {
        this.state = state;
    }
    public String getStateString()
    {
        return this.state;
    }
    public static ConflictState fromString(String stateString)
    {
        for (ConflictState state : ConflictState.values())
        {
            if (stateString.equalsIgnoreCase(state.state))
            {
                return state;
            }
        }
        return null;
    }
}
