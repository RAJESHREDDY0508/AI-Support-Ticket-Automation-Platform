FROM python:3.12-slim

WORKDIR /app

RUN pip install --no-cache-dir --upgrade pip

COPY requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY main.py .
COPY ai_worker/ ai_worker/

ENV PYTHONUNBUFFERED=1
EXPOSE 9090

CMD ["python", "main.py"]
