package samplespannerapplication;

import com.intuit.karate.junit5.Karate;

class SpannerTestRunner {

    @Karate.Test
    Karate testAll() {
        return Karate.run("classpath:samplespannerapplication");
    }

}
