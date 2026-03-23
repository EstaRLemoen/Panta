import sys
import os
import json
import logging

# Ensure the module can find panta
sys.path.append(os.path.abspath(os.path.join(os.path.dirname(__file__), '../..')))

from panta.llm_cfg_analyzer.llm_analyzer import LLMCFGAnalyzer
from panta.model_invocation.llm_invocation import LLMInvocation

logging.basicConfig(level=logging.INFO)

# YOUR CONFIGURATION HERE
API_KEY = "YOUR_API_KEY_HERE"
API_BASE_URL = "YOUR_BASE_URL_HERE"
MODEL_NAME = "gpt-4o-mini" # Or whatever model name your provider uses

def test_analyzer():
    java_code = """
package com.example;

import java.util.List;
import java.util.Map;

public class Calculator {
    private int count = 0;

    public int add(int a, int b) {
        if (a > 0) {
            return a + b;
        } else {
            return b;
        }
    }
    
    public String checkStatus(boolean isReady, int items) {
        if (!isReady) {
            return "Not Ready";
        }
        if (items == 0) {
            return "Empty";
        }
        return "Ready with items";
    }
}
"""

    print("1. Initializing Analyzer with Java Code...")
    analyzer = LLMCFGAnalyzer(src_language="java", src_code=java_code, llm_model=f"openai/{MODEL_NAME}")
    analyzer.extract_deterministic_info()
    
    print("2. Generating Prompt...")
    prompt = analyzer.generate_llm_prompt()
    
    # We set litellm env vars directly here to override any local config 
    os.environ["LITELLM_LOG"] = "INFO"
    
    # Depending on the provider, you might need to set standard openai variables
    # If your proxy is OpenAI compatible:
    os.environ["OPENAI_API_KEY"] = API_KEY
    os.environ["OPENAI_API_BASE"] = API_BASE_URL
    
    # Initialize Panta's LLM invoker
    llm = LLMInvocation(model=f"openai/{MODEL_NAME}") # The openai/ prefix tells litellm to use the OpenAI API format (which your custom base URL likely mimics)
    
    print(f"3. Calling LLM ({MODEL_NAME}) at {API_BASE_URL}...")
    try:
        response_text, _, _ = llm.call_model(prompt=prompt, max_tokens=1024, temperature=0.1)
        print("\n--- Raw LLM Response ---")
        print(response_text)
        print("------------------------\n")
        
        print("4. Parsing LLM Response...")
        analyzer.parse_llm_response(response_text)
        
        print("5. === DUMPING FINAL file_obj ===")
        print(json.dumps(analyzer.file_obj, indent=2))
        
    except Exception as e:
        print(f"Failed to call LLM or parse response: {e}")

if __name__ == "__main__":
    test_analyzer()
