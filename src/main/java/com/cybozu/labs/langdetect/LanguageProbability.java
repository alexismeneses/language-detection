package com.cybozu.labs.langdetect;

import java.util.ArrayList;

/**
 * {@link LanguageProbability} is to store the detected language.
 * {@link Detector#getProbabilities()} returns an {@link ArrayList} of {@link LanguageProbability}s.
 *
 * @see Detector#getProbabilities()
 * @author Nakatani Shuyo
 * @author Alexis Meneses
 *
 */
public class LanguageProbability {
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

    @Override
    public String toString() {
        if (lang==null) return "";
        return lang + ":" + prob;
    }
}
