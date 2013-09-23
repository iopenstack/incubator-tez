/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.tez.engine.api.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.classification.InterfaceAudience.Private;
import org.apache.hadoop.conf.Configuration;
import org.apache.tez.common.counters.TezCounters;
import org.apache.tez.dag.records.TezTaskAttemptID;
import org.apache.tez.engine.api.impl.EventMetaData.EventProducerConsumerType;
import org.apache.tez.engine.newapi.Event;
import org.apache.tez.engine.newapi.TezInputContext;
import org.apache.tez.engine.newruntime.RuntimeTask;

public class TezInputContextImpl extends TezTaskContextImpl
    implements TezInputContext {

  private final byte[] userPayload;
  private final String sourceVertexName;
  private final EventMetaData sourceInfo;

  @Private
  public TezInputContextImpl(Configuration conf, int appAttemptNumber,
      TezUmbilical tezUmbilical, String taskVertexName,
      String sourceVertexName, TezTaskAttemptID taskAttemptID,
      TezCounters counters, byte[] userPayload,
      RuntimeTask runtimeTask, Map<String, ByteBuffer> serviceConsumerMetadata) {
    super(conf, appAttemptNumber, taskVertexName, taskAttemptID,
        counters, runtimeTask, tezUmbilical, serviceConsumerMetadata);
    this.userPayload = userPayload;
    this.sourceVertexName = sourceVertexName;
    this.sourceInfo = new EventMetaData(
        EventProducerConsumerType.INPUT, taskVertexName, sourceVertexName,
        taskAttemptID);
    this.uniqueIdentifier = String.format("%s_%s_%06d_%02d_%s", taskAttemptID
        .getTaskID().getVertexID().getDAGId().toString(), taskVertexName,
        getTaskIndex(), getTaskAttemptNumber(), sourceVertexName);
  }

  @Override
  public void sendEvents(List<Event> events) {
    List<TezEvent> tezEvents = new ArrayList<TezEvent>(events.size());
    for (Event e : events) {
      TezEvent tEvt = new TezEvent(e, sourceInfo);
      tezEvents.add(tEvt);
    }
    tezUmbilical.addEvents(tezEvents);
  }

  @Override
  public byte[] getUserPayload() {
    return userPayload;
  }

  @Override
  public String getSourceVertexName() {
    return sourceVertexName;
  }

  @Override
  public void fatalError(Throwable exception, String message) {
    super.signalFatalError(exception, message, sourceInfo);
  }
}