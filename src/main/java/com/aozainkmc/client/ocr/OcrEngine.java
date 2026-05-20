package com.aozainkmc.client.ocr;

import java.util.List;

public interface OcrEngine extends AutoCloseable {
    List<OcrCandidate> recognize(float[] input, int topK) throws Exception;
}
