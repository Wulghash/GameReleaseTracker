#!/bin/sh
# Fly.io sets DATABASE_URL as postgres://user:pass@host:5432/db
# Spring Boot needs it split into JDBC form.
if [ -n "$DATABASE_URL" ]; then
  # Strip the scheme
  rest="${DATABASE_URL#postgres://}"
  # user:pass@host:port/db
  userpass="${rest%%@*}"
  hostdb="${rest#*@}"
  DB_USER="${userpass%%:*}"
  DB_PASS="${userpass#*:}"
  DB_HOST="${hostdb%%/*}"
  DB_NAME="${hostdb#*/}"

  export SPRING_DATASOURCE_URL="jdbc:postgresql://${DB_HOST}/${DB_NAME}"
  export SPRING_DATASOURCE_USERNAME="$DB_USER"
  export SPRING_DATASOURCE_PASSWORD="$DB_PASS"
fi

exec java -jar app.jar
