package org.elasticmq.rest.sqs

import Constants._
import org.elasticmq.DeliveryReceipt
import org.elasticmq.msg.DeleteMessage
import org.elasticmq.actor.reply._

trait DeleteMessageBatchDirectives { this: ElasticMQDirectives with BatchRequestsModule =>
  val deleteMessageBatch = {
    action("DeleteMessageBatch") {
      queueActorFromPath { queueActor =>
        anyParamsMap { parameters =>
          val resultsFuture = batchRequest("DeleteMessageBatchRequestEntry", parameters) { (messageData, id) =>
            val receiptHandle = messageData(ReceiptHandleParameter)
            val msgId = DeliveryReceipt(receiptHandle).extractId

            val result = queueActor ? DeleteMessage(msgId)

            result.map { _ =>
              <DeleteMessageBatchResultEntry>
                <Id>{id}</Id>
              </DeleteMessageBatchResultEntry>
            }
          }

          resultsFuture.map { results =>
            respondWith {
              <DeleteMessageBatchResponse>
                <DeleteMessageBatchResult>
                  {results}
                </DeleteMessageBatchResult>
                <ResponseMetadata>
                  <RequestId>{EmptyRequestId}</RequestId>
                </ResponseMetadata>
              </DeleteMessageBatchResponse>
            }
          }
        }
      }
    }
  }
}