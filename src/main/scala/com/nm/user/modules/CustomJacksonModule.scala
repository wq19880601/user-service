package com.nm.user.modules

import com.fasterxml.jackson.annotation.JsonInclude.Include
import com.fasterxml.jackson.core.JsonGenerator.Feature
import com.fasterxml.jackson.databind.{ObjectMapper, PropertyNamingStrategy}
import com.twitter.finatra.json.modules.FinatraJacksonModule

object CustomJacksonModule extends FinatraJacksonModule {


  override val serializationInclusion = Include.ALWAYS


  override protected val propertyNamingStrategy = {
    PropertyNamingStrategy.SNAKE_CASE
  }

  override protected def additionalMapperConfiguration(mapper: ObjectMapper) {
    mapper.configure(Feature.WRITE_NUMBERS_AS_STRINGS, false)
  }
}
