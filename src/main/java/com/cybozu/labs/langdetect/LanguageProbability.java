package com.cybozu.labs.langdetect;

import java.util.List;

/**
 * {@link LanguageProbability} is to store the detected language.
 * {@link Detector#getProbabilities()} returns a {@link List} of {@link LanguageProbability}s.
 * <p>
 * {@code LanguageProbability} is self comparable according to their decreasing probability
 * making their collections sortable.
 * @see Detector#getProbabilities()
 * @author Nakatani Shuyo
 * @author Alexis Meneses
 *
 */
public class LanguageProbability implements Comparable<LanguageProbability> {
    private String lang;
    private double prob;

    public LanguageProbability(String lang, double prob) {
        this.lang = lang;
        this.prob = prob;
    }

    public String getLanguage() {
        return lang;
    }

    public double getProbability() {
        return prob;
    }

    public int compareTo(LanguageProbability o) {
        double diff = o.prob - prob;
        if (diff > 0)
        {
            return 1;
        }
        else if (diff < 0)
        {
            return -1;
        }
        else
        {
            return 0;
        }
    }

    @Override
    public String toString() {
        if (lang==null) return "";
        return lang + ":" + prob;
    }
}
