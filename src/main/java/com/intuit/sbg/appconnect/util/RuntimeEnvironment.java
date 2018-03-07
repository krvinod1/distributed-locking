package com.intuit.sbg.appconnect.util;

import org.apache.commons.lang3.StringUtils;


/**
 * Created by vkumar21 on 3/6/18.
 */
public enum RuntimeEnvironment {
        PROD("prod"),
        STAGE("stage"),
        PERF("perf"),
        BETA("beta"),
        QA("qa"),
        DEV("dev");

        private String stack;

        /**
         * @param stack stack name
         */
        RuntimeEnvironment(String stack) {
            this.stack = stack;
        }

        /**
         * Convert an env string to RuntimeEnvironment. If the passed env string is not one of
         * DEV | QA | PERF | STAGE | BETA or PROD, it returns DEV by default
         *
         * @param env environment
         * @return RuntimeEnvironment
         */
        public static RuntimeEnvironment toRunTimeEnvironment(String env) {
            try {
                return RuntimeEnvironment.valueOf(StringUtils.upperCase(env));
            } catch (IllegalArgumentException e) {

                //If it is invalid env string, return DEV environment
                return DEV;
            }
        }

        public String stack() {
            return stack;
        }

        public boolean isProd() {
            return this.equals(PROD);
        }

        public boolean isBeta() {
            return this.equals(BETA);
        }

        public boolean isStage() {
            return this.equals(STAGE);
        }

        public boolean isPerf() {
            return this.equals(PERF);
        }

        public boolean isQA() {
            return this.equals(QA);
        }

        public boolean isDev() {
            return this.equals(DEV);
        }

        public boolean isProdStack() {
            return (isBeta() || isProd());
        }

        public boolean isPreProdStack() {
            return (isStage() || isPerf() || isQA() || isDev());
        }

        public boolean isProdStackAccessingE2E(String url) {
            return (this.isProdStack() && StringUtils.isNotEmpty(url) && (url.contains("qal") || (url.contains("e2e"))));
        }
}
