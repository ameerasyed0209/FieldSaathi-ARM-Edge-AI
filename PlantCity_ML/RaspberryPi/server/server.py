#!/usr/bin/env python3

from http.server import BaseHTTPRequestHandler, HTTPServer
import json
import subprocess
import os
import time


HOST = "0.0.0.0"
PORT = 8000

MODEL_PATH = os.path.expanduser(
    "~/llama-3.2-1b/pretrained/"
    "Llama-3.2-1B-Instruct-8da4w-gptq-2k.pte"
)

TOKENIZER_PATH = os.path.expanduser(
    "~/llama-3.2-1b/original/tokenizer.model"
)

LLAMA_RUNNER = os.path.expanduser(
    "~/executorch/build-native/examples/models/llama/llama_main"
)


def generate_advisory(crop, disease, confidence, question):

    prompt = f"""
You are KrishiSaathi, an offline agricultural advisory assistant.

Crop: {crop}
Detected condition: {disease}
Vision confidence: {confidence}

Farmer's question:
{question}

Give practical agricultural guidance.

Include:
1. What the condition means
2. Immediate actions
3. Recommended management
4. Prevention
5. Important precautions

Keep the answer simple and useful for a farmer.
Do not invent laboratory test results.
Do not claim certainty beyond the supplied crop and disease information.
"""

    command = [
        LLAMA_RUNNER,
        "--model_path",
        MODEL_PATH,
        "--tokenizer_path",
        TOKENIZER_PATH,
        "--prompt",
        prompt,
    ]

    start_time = time.time()

    result = subprocess.run(
        command,
        capture_output=True,
        text=True,
        timeout=120
    )

    inference_time = time.time() - start_time

    if result.returncode != 0:
        raise RuntimeError(
            result.stderr.strip() or "Llama inference failed"
        )

    advisory = result.stdout.strip()

    return advisory, inference_time


class RequestHandler(BaseHTTPRequestHandler):

    def send_json(self, status_code, data):

        response = json.dumps(data, ensure_ascii=False).encode("utf-8")

        self.send_response(status_code)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(response)))
        self.end_headers()

        self.wfile.write(response)

    def do_GET(self):

        if self.path == "/health":

            self.send_json(
                200,
                {
                    "status": "ok",
                    "service": "KrishiSaathi Advisory AI"
                }
            )

        else:

            self.send_json(
                404,
                {
                    "error": "Endpoint not found"
                }
            )

    def do_POST(self):

        if self.path != "/ask":

            self.send_json(
                404,
                {
                    "error": "Endpoint not found"
                }
            )

            return

        try:

            content_length = int(
                self.headers.get("Content-Length", 0)
            )

            body = self.rfile.read(content_length)

            request = json.loads(body.decode("utf-8"))

            crop = request.get("crop", "")
            disease = request.get("disease", "")
            confidence = request.get("confidence", "")
            question = request.get("question", "")

            if not crop:
                raise ValueError("crop is required")

            if not disease:
                raise ValueError("disease is required")

            if not question:
                question = "What should the farmer do?"

            advisory, inference_time = generate_advisory(
                crop,
                disease,
                confidence,
                question
            )

            response = {
                "crop": crop,
                "disease": disease,
                "confidence": confidence,
                "advice": advisory,
                "inference_time_seconds": round(
                    inference_time, 2
                )
            }

            self.send_json(200, response)

        except json.JSONDecodeError:

            self.send_json(
                400,
                {
                    "error": "Invalid JSON request"
                }
            )

        except subprocess.TimeoutExpired:

            self.send_json(
                504,
                {
                    "error": "Advisory generation timed out"
                }
            )

        except Exception as e:

            print(f"ERROR: {e}")

            self.send_json(
                500,
                {
                    "error": str(e)
                }
            )


def main():

    print("=" * 50)
    print("KrishiSaathi Advisory AI")
    print("=" * 50)

    print(f"Model     : {MODEL_PATH}")
    print(f"Tokenizer : {TOKENIZER_PATH}")
    print(f"Runner    : {LLAMA_RUNNER}")
    print(f"Server    : http://{HOST}:{PORT}")

    if not os.path.exists(MODEL_PATH):
        print("WARNING: Model file not found.")

    if not os.path.exists(TOKENIZER_PATH):
        print("WARNING: Tokenizer file not found.")

    if not os.path.exists(LLAMA_RUNNER):
        print("WARNING: ExecuTorch runner not found.")

    server = HTTPServer(
        (HOST, PORT),
        RequestHandler
    )

    print()
    print("Server started.")
    print("GET  /health")
    print("POST /ask")
    print()

    try:
        server.serve_forever()

    except KeyboardInterrupt:

        print("\nStopping server...")

        server.shutdown()


if __name__ == "__main__":
    main()
