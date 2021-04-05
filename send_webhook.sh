#!/bin/bash
WEBHOOK_BODY=`cat webhook_data.json`
curl -X POST -H "Content-Type: application/json" -d "$WEBHOOK_BODY" $DISCORD_WEBHOOK
rm webhook_data.json
