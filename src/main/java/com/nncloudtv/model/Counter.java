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

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.Unique;

import com.nncloudtv.lib.NnStringUtil;

/**
 * Represents a counter in the datastore and stores the number of shards.
 */
@PersistenceCapable(table = "counter", detachable = "true")
public class Counter implements PersistentModel {
    
    private static final long serialVersionUID = -3785260187645061818L;
    private static final boolean cachable = false;
    
    public boolean isCachable() {
        return cachable;
    }
    
    @PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private long id;
    
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    @Unique
    @Persistent
    @Column(jdbcType = NnStringUtil.VARCHAR, length = NnStringUtil.NORMAL_STRING_LENGTH)
    private String counterName;
    
    @Persistent
    private int numShards;
    
    public Counter(String counterName) {
      this.counterName = counterName;
      this.numShards = 0;
    }
    
    public String getCounterName() {
      return counterName;
    }
    
    public int getNumShards() {
        return numShards;
    }
    
    public void setNumShards(int numShards) {
        this.numShards = numShards;
    }
    
    public void setUpdateDate(Date date) {
        // TODO Auto-generated method stub
        
    }
    
    public Date getUpdateDate() {
        // TODO Auto-generated method stub
        return null;
    }
    
    public void setCreateDate(Date date) {
        // TODO Auto-generated method stub
        
    }
    
    public Date getCreateDate() {
        // TODO Auto-generated method stub
        return null;
    }
}
