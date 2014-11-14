/* Copyright (c) 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.nncloudtv.model;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

import com.nncloudtv.lib.NnStringUtil;

/**
 * Represents a counter in the datastore and stores the number of shards.
 *
 */
@PersistenceCapable(table = "counter", identityType = IdentityType.APPLICATION)
public class Counter {
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    @Persistent
    @Unique
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String counterName;
    
    @Persistent
    private int numShards;
    
    @NotPersistent
    private long count;
    
    public Counter(String counterName) {
      this.counterName = counterName;
      this.numShards = 0;
    }
    
    public long getId() {
      return id;
    }
    
    public String getCounterName() {
      return counterName;
    }
    
    public long getCount() {
        return count;
    }
    
    public void setCount(long count) {
        this.count = count;
    }
    
    public int getNumShards() {
        return numShards;
    }
    
    public void setNumShards(int numShards) {
        this.numShards = numShards;
    }
}