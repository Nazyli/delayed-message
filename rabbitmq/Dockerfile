#FROM rabbitmq:management
FROM rabbitmq:3.8.8-management

# Salin plugin ke dalam container
COPY rabbitmq_delayed_message_exchange-4.0.2.ez /plugins/

# Aktifkan plugin
RUN rabbitmq-plugins enable --offline rabbitmq_delayed_message_exchange
