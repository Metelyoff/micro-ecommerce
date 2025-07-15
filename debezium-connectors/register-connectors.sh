#!/bin/sh
echo 'Waiting for Debezium REST API...'
until curl -s http://debezium:8083/connectors > /dev/null; do
  echo '...still waiting...'
  sleep 2
done

echo 'Debezium is ready. Registering connectors...'
for f in /connectors/*.json; do
  name=$(grep -o '"name"[[:space:]]*:[[:space:]]*"[^"]*"' "$f" | head -n1 | cut -d':' -f2 | tr -d ' "')
  echo "Registering connector: $name"
  response=$(curl -s -o /dev/null -w "%{http_code}" -X POST \
    -H "Content-Type: application/json" \
    --data @"$f" http://debezium:8083/connectors)

  if [ "$response" -eq 200 ]; then
    echo "Registered $name"
  elif [ "$response" -eq 409 ]; then
    echo "Connector $name already exists"
  else
    echo "Failed to register $name (HTTP $response)"
  fi
done