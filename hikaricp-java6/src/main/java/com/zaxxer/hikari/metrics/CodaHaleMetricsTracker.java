/*
 * Copyright (C) 2013,2014 Brett Wooldridge
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.zaxxer.hikari.metrics;

import com.codahale.metrics.Histogram;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.zaxxer.hikari.pool.HikariPool;

public final class CodaHaleMetricsTracker extends MetricsTracker
{
   private MetricRegistry registry;
   private final Timer connectionObtainTimer;
   private final Histogram connectionUsage;

   public CodaHaleMetricsTracker(final HikariPool pool)
   {
      super(pool);

      registry = new MetricRegistry();
      connectionObtainTimer = registry.timer(MetricRegistry.name(pool.getConfiguration().getPoolName(), "-connection", "Wait"));
      connectionUsage = registry.histogram(MetricRegistry.name(pool.getConfiguration().getPoolName(), "-connection", "Wsage"));
   }

   /** {@inheritDoc} */
   @Override
   public Context recordConnectionRequest(long requestTime)
   {
      return new Context(connectionObtainTimer);
   }

   /** {@inheritDoc} */
   @Override
   public void recordConnectionUsage(long usageMilleseconds)
   {
      connectionUsage.update(usageMilleseconds);
   }

   public Timer getConnectionAcquisitionTimer()
   {
      return connectionObtainTimer;
   }

   public Histogram getConnectionDurationHistogram()
   {
      return connectionUsage;
   }

   public static final class Context extends MetricsContext
   {
      final Timer.Context innerContext;

      Context(Timer timer)
      {
         innerContext = timer.time();
      }

      public void stop()
      {
         if (innerContext != null) {
            innerContext.stop();
         }
      }
   }
}
