use nncloudtv_content;

      select c.numShards,sum(count) sum,s.counterName
        from counter_shard s
  inner join (select counterName,numShards from counter where numShards > 1) c
          on c.counterName = s.counterName
    group by s.counterName
    order by sum desc
       limit 20;

