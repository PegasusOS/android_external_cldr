package org.unicode.cldr.util;

import java.util.Collections;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.ibm.icu.util.ULocale;

/**
 * This is a first-cut version just to get going. The data is hard coded until we switch over to real data in ICU.
 */
public class DayPeriods {
    public static final int HOUR = 60 * 60 * 1000;

    public enum DayPeriod {
        MORNING1("EARLY_MORNING"), MORNING2("MORNING"), AFTERNOON1("EARLY_AFTERNOON"), AFTERNOON2("AFTERNOON"), EVENING1("EARLY_EVENING"), EVENING2(
            "EVENING"), NIGHT1("NIGHT"), NIGHT2("LATE_NIGHT");
        public final String name;

        DayPeriod(String name) {
            this.name = name;
        }

        public static DayPeriod get(String dayPeriod) {
            for (DayPeriod d : DayPeriod.values()) {
                if (dayPeriod.equals(d.name)) {
                    return d;
                }
            }
            return DayPeriod.valueOf(dayPeriod);
        }
    }

    /**
     * Get the category for a given time in the day.
     * @param millisInDay
     * @return
     */
    public DayPeriod get(long millisInDay) {
        long hours = millisInDay / HOUR;
        int hoursInDay = (int) (hours % 24);
        if (hoursInDay < 0) {
            hoursInDay += 24;
        }
        return timeMap[hoursInDay];
    }

    /**
     * Get the *actual* locale for the DayPeriods (eg, asking for "en-AU" may get you "en")
     * @param millisInDay
     * @return
     */
    public ULocale getLocale() {
        return locale;
    }

    /**
     * Get a sample, for showing to a localizer. The actual phrase should come out of a SELECT statement, since it may vary by message.
     * @param millisInDay
     * @return
     */
    public String getSample(DayPeriod dayPeriod) {
        return samples.get(dayPeriod);
    }

    /**
     * Return the possible DayPeriod values for this locale.
     * @return
     */
    public Set<DayPeriod> getDayPeriods() {
        return samples.keySet();
    }

    /**
     * Get an instance with a factory method. Right now, returns null if the locale data is not available.
     * @param loc
     * @return
     */
    public static DayPeriods getInstance(ULocale loc) {
        ULocale base = new ULocale(loc.getLanguage());
        DayPeriods result = DATA.get(base);
//        if (result == null) {
//            throw new IllegalArgumentException("No data for locale " + loc);
//        }
        return result;
    }

    /**
     * Returns the available locales. Note that regional/script variants may be mapped by getInstance to a base locale,
     * eg, en-AU => en.
     * @return
     */
    public static Set<ULocale> getAvailable() {
        return DATA.keySet();
    }

    // ===== PRIVATES =====

    private final ULocale locale;
    private final DayPeriod[] timeMap;
    private final Map<DayPeriod, String> samples;

    private DayPeriods(ULocale base, DayPeriod[] map, EnumMap<DayPeriod, String> samples2) {
        locale = base;
        fix(map, samples2, DayPeriod.MORNING2, DayPeriod.MORNING1);
        fix(map, samples2, DayPeriod.AFTERNOON2, DayPeriod.AFTERNOON1);
        fix(map, samples2, DayPeriod.EVENING2, DayPeriod.EVENING1);
        fix(map, samples2, DayPeriod.NIGHT2, DayPeriod.NIGHT1);
        timeMap = map;
        samples = Collections.unmodifiableMap(samples2);
    }

    private void fix(DayPeriod[] map, EnumMap<DayPeriod, String> samples2, DayPeriod dayPeriod2, DayPeriod dayPeriod1) {
        if (samples2.containsKey(dayPeriod2) && !samples2.containsKey(dayPeriod1)) {
            samples2.put(dayPeriod1, samples2.get(dayPeriod2));
            samples2.remove(dayPeriod2);
            for (int i = 0; i < map.length; ++i) {
                if (map[i] == dayPeriod2) {
                    map[i] = dayPeriod1;
                }
            }
        }
    }

    // HACK TO SET UP DATA
    // Will be replaced by real data table in the future

    private static final Map<ULocale, DayPeriods> DATA = new LinkedHashMap<>();

    private static DayPeriodBuilder make(String locale) {
        return new DayPeriodBuilder(locale);
    }

    private static class DayPeriodBuilder {
        private final ULocale locale;
        private final DayPeriod[] timeMap = new DayPeriod[24];
        private final EnumMap<DayPeriod, String> samples = new EnumMap<>(DayPeriod.class);

        DayPeriodBuilder(String locale) {
            this.locale = new ULocale(locale);
        }

        public DayPeriodBuilder add(String dayPeriod, String localeName, int... hours) {
            DayPeriod dayPeriodEnum = DayPeriod.get(dayPeriod);
            String previous = samples.put(dayPeriodEnum, localeName);
//            if (previous != null) {
//                throw new IllegalArgumentException(locale + " Collision");
//            }
            for (int i : hours) {
                if (timeMap[i] != null) {
                    throw new IllegalArgumentException(locale + " Collision " + i + ", " + timeMap[i] + ", " + dayPeriodEnum);
                }
                timeMap[i] = dayPeriodEnum;
            }
            return this;
        }

        public DayPeriods build() {
            for (int i = 0; i < timeMap.length; ++i) {
                DayPeriod dp = timeMap[i];
                if (dp == null) {
                    throw new IllegalArgumentException(locale + " Missing item: " + i);
                }
            }
            DayPeriods item = new DayPeriods(locale, timeMap, samples);
            DATA.put(locale, item);
            return item;
        }
    }

    static {
        make("en")
            .add("NIGHT1", "night", 0, 1, 2, 3)
            .add("MORNING1", "morning", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "afternoon", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "evening", 18, 19, 20)
            .add("NIGHT1", "night", 21, 22, 23)
            .build();

        make("af")
            .add("NIGHT1", "nag", 0, 1, 2, 3, 4)
            .add("MORNING1", "oggend", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "middag", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "aand", 18, 19, 20, 21, 22, 23)
            .build();

        make("nl")
            .add("NIGHT1", "nacht", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "ochtend", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "middag", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "avond", 18, 19, 20, 21, 22, 23)
            .build();

        make("de")
            .add("NIGHT1", "Nacht", 0, 1, 2, 3, 4)
            .add("MORNING1", "Morgen", 5, 6, 7, 8, 9)
            .add("MORNING2", "Vormittag", 10, 11)
            .add("AFTERNOON1", "Mittag", 12)
            .add("AFTERNOON2", "Nachmittag", 13, 14, 15, 16, 17)
            .add("EVENING1", "Abend", 18, 19, 20, 21, 22, 23)
            .build();

        make("da")
            .add("NIGHT1", "nat", 0, 1, 2, 3, 4)
            .add("MORNING1", "morgen", 5, 6, 7, 8, 9)
            .add("MORNING2", "formiddag", 10, 11)
            .add("AFTERNOON1", "eftermiddag", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "aften", 18, 19, 20, 21, 22, 23)
            .build();

        make("nb")
            .add("NIGHT1", "natt", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "morgen", 6, 7, 8, 9)
            .add("MORNING2", "formiddag", 10, 11)
            .add("AFTERNOON1", "ettermiddag", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "kveld", 18, 19, 20, 21, 22, 23)
            .build();

        make("sv")
            .add("NIGHT1", "natt", 0, 1, 2, 3, 4)
            .add("MORNING1", "morgon", 5, 6, 7, 8, 9)
            .add("MORNING2", "f??rmiddag", 10, 11)
            .add("AFTERNOON1", "eftermiddag", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "kv??ll", 18, 19, 20, 21, 22, 23)
            .build();

        make("is")
            .add("NIGHT1", "n??tt", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "morgunn", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "eftir h??degi", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "kv??ld", 18, 19, 20, 21, 22, 23)
            .build();

        make("pt")
            .add("NIGHT1", "madrugada", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "manh??", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "tarde", 12, 13, 14, 15, 16, 17, 18)
            .add("EVENING1", "noite", 19, 20, 21, 22, 23)
            .build();

        make("gl")
            .add("MORNING1", "madrugada", 0, 1, 2, 3, 4, 5)
            .add("MORNING2", "ma????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "mediod??a", 12)
            .add("EVENING1", "tarde", 13, 14, 15, 16, 17, 18, 19, 20)
            .add("NIGHT1", "noite", 21, 22, 23)
            .build();

        make("es")
            .add("MORNING1", "madrugada", 0, 1, 2, 3, 4, 5)
            .add("MORNING2", "ma??ana", 6, 7, 8, 9, 10, 11)
            .add("EVENING1", "tarde", 12, 13, 14, 15, 16, 17, 18, 19)
            .add("NIGHT1", "noche", 20, 21, 22, 23)
            .build();

        make("ca")
            .add("MORNING1", "matinada", 0, 1, 2, 3, 4, 5)
            .add("MORNING2", "mat??", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "migdia", 12)
            .add("AFTERNOON2", "tarda", 13, 14, 15, 16, 17, 18)
            .add("EVENING1", "vespre", 19, 20)
            .add("NIGHT1", "nit", 21, 22, 23)
            .build();

        make("it")
            .add("NIGHT1", "notte", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "mattina", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "pomeriggio", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "sera", 18, 19, 20, 21, 22, 23)
            .build();

        make("ro")
            .add("NIGHT1", "noapte", 0, 1, 2, 3, 4)
            .add("MORNING1", "diminea????", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "dup??-amiaz??", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "sear??", 18, 19, 20, 21)
            .add("NIGHT1", "noapte", 22, 23)
            .build();

        make("fr")
            .add("NIGHT1", "nuit", 0, 1, 2, 3)
            .add("MORNING1", "matin", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "apr??s-midi", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "soir", 18, 19, 20, 21, 22, 23)
            .build();

        make("hr")
            .add("NIGHT1", "no??", 0, 1, 2, 3)
            .add("MORNING1", "jutro", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "popodne", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "ve??er", 18, 19, 20)
            .add("NIGHT1", "no??", 21, 22, 23)
            .build();

        make("bs")
            .add("NIGHT1", "no??", 0, 1, 2, 3)
            .add("MORNING1", "jutro", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "popodne", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "ve??e", 18, 19, 20)
            .add("NIGHT1", "no??", 21, 22, 23)
            .build();

        make("sr")
            .add("NIGHT1", "??????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "??????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "????????", 18, 19, 20)
            .add("NIGHT1", "??????", 21, 22, 23)
            .build();

        make("sl")
            .add("NIGHT1", "no??", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "jutro", 6, 7, 8, 9)
            .add("MORNING2", "dopoldne", 10, 11)
            .add("AFTERNOON1", "popoldne", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "ve??er", 18, 19, 20, 21)
            .add("NIGHT1", "no??", 22, 23)
            .build();

        make("cs")
            .add("NIGHT1", "noc", 0, 1, 2, 3)
            .add("MORNING1", "r??no", 4, 5, 6, 7, 8)
            .add("MORNING2", "dopoledne", 9, 10, 11)
            .add("AFTERNOON1", "odpoledne", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "ve??er", 18, 19, 20, 21)
            .add("NIGHT1", "noc", 22, 23)
            .build();

        make("sk")
            .add("NIGHT1", "noc", 0, 1, 2, 3)
            .add("MORNING1", "r??no", 4, 5, 6, 7, 8)
            .add("MORNING2", "dopoludnie", 9, 10, 11)
            .add("AFTERNOON1", "popoludnie", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "ve??er", 18, 19, 20, 21)
            .add("NIGHT1", "noc", 22, 23)
            .build();

        make("pl")
            .add("NIGHT1", "noc", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "rano", 6, 7, 8, 9)
            .add("MORNING2", "przedpo??udnie", 10, 11)
            .add("AFTERNOON1", "popo??udnie", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "wiecz??r", 18, 19, 20)
            .add("NIGHT1", "noc", 21, 22, 23)
            .build();

        make("bg")
            .add("NIGHT1", "??????", 0, 1, 2, 3)
            .add("MORNING1", "????????????????", 4, 5, 6, 7, 8, 9, 10)
            .add("MORNING2", "???? ????????", 11, 12, 13)
            .add("AFTERNOON1", "????????????????", 14, 15, 16, 17)
            .add("EVENING1", "??????????????", 18, 19, 20, 21)
            .add("NIGHT1", "??????", 22, 23)
            .build();

        make("mk")
            .add("NIGHT1", "???? ????????????", 0, 1, 2, 3)
            .add("MORNING1", "????????????", 4, 5, 6, 7, 8, 9)
            .add("MORNING2", "????????????????????", 10, 11)
            .add("AFTERNOON1", "????????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??????????????", 18, 19, 20, 21, 22, 23)
            .build();

        make("ru")
            .add("NIGHT1", "????????", 0, 1, 2, 3)
            .add("MORNING1", "????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??????????", 18, 19, 20, 21, 22, 23)
            .build();

        make("uk")
            .add("NIGHT1", "??????", 0, 1, 2, 3)
            .add("MORNING1", "??????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??????????", 18, 19, 20, 21, 22, 23)
            .build();

        make("lt")
            .add("NIGHT1", "naktis", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "rytas", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "diena", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "vakaras", 18, 19, 20, 21, 22, 23)
            .build();

        make("lv")
            .add("NIGHT1", "nakts", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "r??ts", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "p??cpusdiena", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "vakars", 18, 19, 20, 21, 22)
            .add("NIGHT1", "nakts", 23)
            .build();

        make("el")
            .add("NIGHT1", "??????????", 0, 1, 2, 3)
            .add("MORNING1", "????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????????????", 12, 13, 14, 15, 16)
            .add("EVENING1", "????????????????", 17, 18, 19)
            .add("NIGHT1", "??????????", 20, 21, 22, 23)
            .build();

        make("fa")
            .add("NIGHT1", "????", 0, 1, 2, 3)
            .add("MORNING1", "??????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "?????? ???? ??????", 12, 13, 14, 15, 16)
            .add("EVENING1", "??????", 17, 18)
            .add("NIGHT1", "????", 19, 20, 21, 22, 23)
            .build();

        make("hy")
            .add("NIGHT1", "??????????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??????????", 18, 19, 20, 21, 22, 23)
            .build();

        make("ka")
            .add("NIGHT1", "????????????", 0, 1, 2, 3, 4)
            .add("MORNING1", "????????????", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????????????????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??????????????????", 18, 19, 20)
            .add("NIGHT1", "????????????", 21, 22, 23)
            .build();

        make("sq")
            .add("NIGHT1", "nat??", 0, 1, 2, 3)
            .add("MORNING1", "m??ngjes", 4, 5, 6, 7, 8)
            .add("MORNING2", "paradite", 9, 10, 11)
            .add("AFTERNOON1", "pasdite", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "mbr??mje", 18, 19, 20, 21, 22, 23)
            .build();

        make("ur")
            .add("NIGHT1", "??????", 0, 1, 2, 3)
            .add("MORNING1", "??????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????", 12, 13, 14, 15)
            .add("AFTERNOON2", "???? ??????", 16, 17)
            .add("EVENING1", "??????", 18, 19)
            .add("NIGHT1", "??????", 20, 21, 22, 23)
            .build();

        make("hi")
            .add("NIGHT1", "?????????", 0, 1, 2, 3)
            .add("MORNING1", "????????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "???????????????", 12, 13, 14, 15)
            .add("EVENING1", "?????????", 16, 17, 18, 19)
            .add("NIGHT1", "?????????", 20, 21, 22, 23)
            .build();

        make("bn")
            .add("NIGHT1", "??????????????????", 0, 1, 2, 3)
            .add("MORNING1", "?????????", 4, 5)
            .add("MORNING2", "????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "???????????????", 12, 13, 14, 15)
            .add("AFTERNOON2", "???????????????", 16, 17)
            .add("EVENING1", "?????????????????????", 18, 19)
            .add("NIGHT1", "??????????????????", 20, 21, 22, 23)
            .build();

        make("gu")
            .add("NIGHT1", "?????????", 0, 1, 2, 3)
            .add("MORNING1", "????????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????????", 12, 13, 14, 15)
            .add("EVENING1", "????????????", 16, 17, 18, 19)
            .add("NIGHT1", "?????????", 20, 21, 22, 23)
            .build();

        make("mr")
            .add("NIGHT1", "??????????????????", 0, 1, 2)
            .add("NIGHT2", "???????????????", 3)
            .add("MORNING1", "???????????????", 4, 5)
            .add("MORNING2", "???????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????????????", 12, 13, 14, 15)
            .add("EVENING1", "??????????????????????????????", 16, 17, 18, 19)
            .add("NIGHT1", "??????????????????", 20, 21, 22, 23)
            .build();

        make("ne")
            .add("NIGHT1", "?????????", 0, 1, 2, 3)
            .add("MORNING1", "???????????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "?????????????????????", 12, 13, 14, 15)
            .add("AFTERNOON2", "????????????", 16, 17, 18)
            .add("EVENING1", "??????????????????", 19, 20, 21)
            .add("NIGHT1", "?????????", 22, 23)
            .build();

        make("pa")
            .add("NIGHT1", "?????????", 0, 1, 2, 3)
            .add("MORNING1", "????????????", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????????????", 12, 13, 14, 15)
            .add("EVENING1", "?????????", 16, 17, 18, 19, 20)
            .add("NIGHT1", "?????????", 21, 22, 23)
            .build();

        make("si")
            .add("NIGHT2", "????????????????????? ?????????", 0)
            .add("MORNING1", "??????????????????", 1, 2, 3, 4, 5)
            .add("MORNING2", "?????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????????", 12, 13)
            .add("EVENING1", "?????????", 14, 15, 16, 17)
            .add("NIGHT1", "??????", 18, 19, 20, 21, 22, 23)
            .build();

        make("ta")
            .add("NIGHT1", "????????????", 0, 1, 2)
            .add("MORNING1", "?????????????????????", 3, 4)
            .add("MORNING2", "????????????", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????????????", 12, 13)
            .add("AFTERNOON2", "????????????????????????", 14, 15)
            .add("EVENING1", "????????????", 16, 17)
            .add("EVENING2", "??????????????? ????????????", 18, 19, 20)
            .add("NIGHT1", "????????????", 21, 22, 23)
            .build();

        make("te")
            .add("NIGHT1", "??????????????????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "???????????????????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "????????????????????????", 18, 19, 20)
            .add("NIGHT1", "??????????????????", 21, 22, 23)
            .build();

        make("ml")
            .add("NIGHT1", "??????????????????", 0, 1, 2)
            .add("MORNING1", "????????????????????????", 3, 4, 5)
            .add("MORNING2", "??????????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????????????????????????", 12, 13)
            .add("AFTERNOON2", "????????????????????????????????????", 14)
            .add("EVENING1", "??????????????????????????????", 15, 16, 17)
            .add("EVENING2", "????????????????????????????????????", 18)
            .add("NIGHT1", "??????????????????", 19, 20, 21, 22, 23)
            .build();

        make("kn")
            .add("NIGHT1", "??????????????????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "?????????????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????????????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "????????????", 18, 19, 20)
            .add("NIGHT1", "??????????????????", 21, 22, 23)
            .build();

        make("zh")
            .add("NIGHT1", "??????", 0, 1, 2, 3, 4)
            .add("MORNING1", "??????", 5, 6, 7)
            .add("MORNING2", "??????", 8, 9, 10, 11)
            .add("AFTERNOON1", "??????", 12)
            .add("AFTERNOON2", "??????", 13, 14, 15, 16, 17, 18)
            .add("EVENING1", "??????", 19, 20, 21, 22, 23)
            .build();

        make("ja")
            .add("NIGHT2", "??????", 0, 1, 2, 3)
            .add("MORNING1", "???", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "???", 12, 13, 14, 15)
            .add("EVENING1", "??????", 16, 17, 18)
            .add("NIGHT1", "???", 19, 20, 21, 22)
            .add("NIGHT2", "??????", 23)
            .build();

        make("ko")
            .add("NIGHT1", "???", 0, 1, 2)
            .add("MORNING1", "??????", 3, 4, 5)
            .add("MORNING2", "??????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??????", 18, 19, 20)
            .add("NIGHT1", "???", 21, 22, 23)
            .build();

        make("tr")
            .add("NIGHT1", "gece", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "sabah", 6, 7, 8, 9, 10)
            .add("MORNING2", "????leden ??nce", 11)
            .add("AFTERNOON1", "????leden sonra", 12, 13, 14, 15, 16, 17)
            .add("AFTERNOON2", "ak??am??st??", 18)
            .add("EVENING1", "ak??am", 19, 20)
            .add("NIGHT1", "gece", 21, 22, 23)
            .build();

        make("az")
            .add("NIGHT2", "gec??", 0, 1, 2, 3)
            .add("MORNING1", "s??bh", 4, 5)
            .add("MORNING2", "s??h??r", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "g??nd??z", 12, 13, 14, 15, 16)
            .add("EVENING1", "ax??am??st??", 17, 18)
            .add("NIGHT1", "ax??am", 19, 20, 21, 22, 23)
            .build();

        make("kk")
            .add("NIGHT1", "??????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "??????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??????", 18, 19, 20)
            .add("NIGHT1", "??????", 21, 22, 23)
            .build();

        make("ky")
            .add("NIGHT1", "??????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "?????????? ??????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "???????????? ??????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "????????????????", 18, 19, 20)
            .add("NIGHT1", "??????", 21, 22, 23)
            .build();

        make("uz")
            .add("NIGHT1", "tun", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "ertalab", 6, 7, 8, 9, 10)
            .add("AFTERNOON1", "kunduzi", 11, 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "kechqurun", 18, 19, 20, 21)
            .add("NIGHT1", "tun", 22, 23)
            .build();

        make("et")
            .add("NIGHT1", "????", 0, 1, 2, 3, 4)
            .add("MORNING1", "hommik", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "p??rastl??una", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??htu", 18, 19, 20, 21, 22)
            .add("NIGHT1", "????", 23)
            .build();

        make("fi")
            .add("NIGHT1", "y??", 0, 1, 2, 3, 4)
            .add("MORNING1", "aamu", 5, 6, 7, 8, 9)
            .add("MORNING2", "aamup??iv??", 10, 11)
            .add("AFTERNOON1", "iltap??iv??", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "ilta", 18, 19, 20, 21, 22)
            .add("NIGHT1", "y??", 23)
            .build();

        make("hu")
            .add("NIGHT1", "??jjel", 0, 1, 2, 3)
            .add("NIGHT2", "hajnal", 4, 5)
            .add("MORNING1", "reggel", 6, 7, 8)
            .add("MORNING2", "d??lel??tt", 9, 10, 11)
            .add("AFTERNOON1", "d??lut??n", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "este", 18, 19, 20)
            .add("NIGHT1", "??jjel", 21, 22, 23)
            .build();

        make("th")
            .add("NIGHT1", "?????????????????????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "????????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????????????", 12)
            .add("AFTERNOON2", "????????????", 13, 14, 15)
            .add("EVENING1", "????????????", 16, 17)
            .add("EVENING2", "?????????", 18, 19, 20)
            .add("NIGHT1", "?????????????????????", 21, 22, 23)
            .build();

        make("lo")
            .add("NIGHT1", "????????????????????????", 0, 1, 2, 3, 4)
            .add("MORNING1", "??????????????????", 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "?????????", 12, 13, 14, 15)
            .add("EVENING1", "?????????", 16)
            .add("EVENING2", "????????????", 17, 18, 19)
            .add("NIGHT1", "????????????????????????", 20, 21, 22, 23)
            .build();

        make("ar")
            .add("NIGHT1", "?????????? ??????????", 0)
            .add("NIGHT2", "????????", 1, 2)
            .add("MORNING1", "????????", 3, 4, 5)
            .add("MORNING2", "??????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????", 12)
            .add("AFTERNOON2", "?????? ??????????", 13, 14, 15, 16, 17)
            .add("EVENING1", "????????", 18, 19, 20, 21, 22, 23)
            .build();

        make("he")
            .add("NIGHT1", "????????", 0, 1, 2, 3, 4)
            .add("MORNING1", "????????", 5, 6, 7, 8, 9, 10)
            .add("AFTERNOON1", "????????????", 11, 12, 13, 14)
            .add("AFTERNOON2", "?????? ??????????????", 15, 16, 17)
            .add("EVENING1", "??????", 18, 19, 20, 21)
            .add("NIGHT1", "????????", 22, 23)
            .build();

        make("id")
            .add("MORNING1", "pagi", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9)
            .add("AFTERNOON1", "siang", 10, 11, 12, 13, 14)
            .add("EVENING1", "sore", 15, 16, 17)
            .add("NIGHT1", "malam", 18, 19, 20, 21, 22, 23)
            .build();

        make("ms")
            .add("MORNING1", "tengah malam", 0)
            .add("MORNING2", "pagi", 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "tengah hari", 12, 13)
            .add("EVENING1", "petang", 14, 15, 16, 17, 18)
            .add("NIGHT1", "malam", 19, 20, 21, 22, 23)
            .build();

        make("fil")
            .add("MORNING1", "madaling-araw", 0, 1, 2, 3, 4, 5)
            .add("MORNING2", "umaga", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "tanghali", 12, 13, 14, 15)
            .add("EVENING1", "hapon", 16, 17)
            .add("NIGHT1", "gabi", 18, 19, 20, 21, 22, 23)
            .build();

        make("vi")
            .add("NIGHT1", "????m", 0, 1, 2, 3)
            .add("MORNING1", "s??ng", 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "chi???u", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "t???i", 18, 19, 20)
            .add("NIGHT1", "????m", 21, 22, 23)
            .build();

        make("km")
            .add("MORNING1", "???????????????", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "???????????????", 18, 19, 20)
            .add("NIGHT1", "?????????", 21, 22, 23)
            .build();

        make("sw")
            .add("NIGHT1", "usiku", 0, 1, 2, 3)
            .add("MORNING1", "alfajiri", 4, 5, 6)
            .add("MORNING2", "asubuhi", 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "mchana", 12, 13, 14, 15)
            .add("EVENING1", "jioni", 16, 17, 18)
            .add("NIGHT1", "usiku", 19, 20, 21, 22, 23)
            .build();

        make("zu")
            .add("MORNING1", "ntathakusa", 0, 1, 2, 3, 4, 5)
            .add("MORNING2", "ekuseni", 6, 7, 8, 9)
            .add("AFTERNOON1", "emini", 10, 11, 12)
            .add("EVENING1", "ntambama", 13, 14, 15, 16, 17, 18)
            .add("NIGHT1", "ebusuku", 19, 20, 21, 22, 23)
            .build();

        make("am")
            .add("NIGHT1", "?????????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "?????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "???????????? ?????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "??????", 18, 19, 20, 21, 22, 23)
            .build();

        make("eu")
            .add("MORNING1", "goizaldea", 0, 1, 2, 3, 4, 5)
            .add("MORNING2", "goiza", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "eguerdia", 12, 13)
            .add("AFTERNOON2", "arratsaldea", 14, 15, 16, 17, 18)
            .add("EVENING1", "iluntzea", 19, 20)
            .add("NIGHT1", "gaua", 21, 22, 23)
            .build();

        make("mn")
            .add("NIGHT1", "????????", 0, 1, 2, 3, 4, 5)
            .add("MORNING1", "??????????", 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "????????", 12, 13, 14, 15, 16, 17)
            .add("EVENING1", "????????", 18, 19, 20)
            .add("NIGHT1", "????????", 21, 22, 23)
            .build();

        make("my")
            .add("MORNING1", "???????????????", 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11)
            .add("AFTERNOON1", "??????????????????", 12, 13, 14, 15)
            .add("EVENING1", "?????????", 16, 17, 18)
            .add("NIGHT1", "???", 19, 20, 21, 22, 23)
            .build();
    }
}
