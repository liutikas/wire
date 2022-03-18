/*
 * Copyright 2021 Square Inc.
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
package com.squareup.wire.reflector

import com.google.protobuf.DescriptorProtos.FileDescriptorProto
import com.squareup.wire.schema.Location
import com.squareup.wire.schema.Schema
import com.squareup.wire.schema.SchemaLoader
import com.squareup.wire.testing.UnwantedValueStripper
import grpc.reflection.v1alpha.ServerReflectionRequest
import grpc.reflection.v1alpha.ServerReflectionResponse
import okio.ByteString.Companion.decodeBase64
import okio.FileSystem
import org.assertj.core.api.Assertions.assertThat
import org.junit.Test

// Reference golang reflection tests https://github.com/juliaogris/reflect
internal class GrpcurlProto2InteropTest {
  @Test
  fun `list services`() {
    val schema = loadSchema()
    // See golden test file https://github.com/juliaogris/reflect/blob/v0.0.7/testdata/proto2-base64/TestServicesCmd.base64
    val respBase64 = "Cgtsb2NhbGhvc3Q6MBIPCgtsb2NhbGhvc3Q6MDoAMjoKDAoKZWNobzIuRWNobwoqCihncnBjLnJlZmxlY3Rpb24udjFhbHBoYS5TZXJ2ZXJSZWZsZWN0aW9u=="
    val expectedResponse = ServerReflectionResponse.ADAPTER.decode(respBase64.decodeBase64()!!)
    assertThat(
      SchemaReflector(schema, includeDependencies = false).process(
        ServerReflectionRequest(
          list_services = "",
          host = "localhost:0"
        )
      )
    ).isEqualTo(expectedResponse)
  }

  val ECHO2_FILEDESCRIPTOR_RESPONSE = "Cg5sb2NhbGhvc3Q6OTA5MBIcCg5sb2NhbGhvc3Q6OTA5MCIKZWNobzIuRWNobyLxTwr5CgoRZWNobzIvZWNobzIucHJvdG8SBWVjaG8yGhxnb29nbGUvYXBpL2Fubm90YXRpb25zLnByb3RvGhlnb29nbGUvcHJvdG9idWYvYW55LnByb3RvIlsKDEhlbGxvUmVxdWVzdBIYCgdtZXNzYWdlGAEgAigJUgdtZXNzYWdlEjEKDG1vcmVfZGV0YWlscxgCIAEoCzIOLmVjaG8yLkRldGFpbHNSC21vcmVEZXRhaWxzIjYKDUhlbGxvUmVzcG9uc2USJQoOcm9ib3RfcmVzcG9uc2UYASACKAlSDXJvYm90UmVzcG9uc2Ui/gQKB0RldGFpbHMSPwoLbGFiZWxfY291bnQYASADKAsyHi5lY2hvMi5EZXRhaWxzLkxhYmVsQ291bnRFbnRyeVIKbGFiZWxDb3VudBIvCgpjb2xvcl90eXBlGAIgAigOMhAuZWNobzIuQ29sb3JUeXBlUgljb2xvclR5cGUSJgoDYW55GAMgAigLMhQuZ29vZ2xlLnByb3RvYnVmLkFueVIDYW55EjkKDW5vdGlmaWNhdGlvbnMYBCADKAsyEy5lY2hvMi5Ob3RpZmljYXRpb25SDW5vdGlmaWNhdGlvbnMSFwoHYV9pbnQzMhgFIAEoBVIGYUludDMyEhkKCGFfdWludDMyGAYgASgNUgdhVWludDMyEhcKB2FfaW50NjQYByABKANSBmFJbnQ2NBIZCghhX3VpbnQ2NBgIIAEoBFIHYVVpbnQ2NBIVCgZhX2Jvb2wYCSABKAhSBWFCb29sEhkKCGFfc2ludDMyGAogASgRUgdhU2ludDMyEhkKCGFfc2ludDY0GAsgASgSUgdhU2ludDY0EhkKCGFfc3RyaW5nGAwgASgJUgdhU3RyaW5nEhcKB2FfYnl0ZXMYDSABKAxSBmFCeXRlcxIbCglhX2ZpeGVkMzIYDiABKAdSCGFGaXhlZDMyEh0KCmFfc2ZpeGVkMzIYDyABKA9SCWFTZml4ZWQzMhIbCglhX2ZpeGVkNjQYECABKAZSCGFGaXhlZDY0Eh0KCmFfc2ZpeGVkNjQYESABKBBSCWFTZml4ZWQ2NBo9Cg9MYWJlbENvdW50RW50cnkSEAoDa2V5GAEgASgJUgNrZXkSFAoFdmFsdWUYAiABKANSBXZhbHVlOgI4ASKZAQoMTm90aWZpY2F0aW9uEg4KAmlkGAEgAigFUgJpZBI2Cgdwcml2YXRlGAIgASgLMhouZWNobzIuUHJpdmF0ZU5vdGlmaWNhdGlvbkgAUgdwcml2YXRlEjMKBnB1YmxpYxgDIAEoCzIZLmVjaG8yLlB1YmxpY05vdGlmaWNhdGlvbkgAUgZwdWJsaWNCDAoKaW5zdHJ1bWVudCI8ChNQcml2YXRlTm90aWZpY2F0aW9uEiUKDnNlY3JldF9jb250ZW50GAEgAigJUg1zZWNyZXRDb250ZW50Ii4KElB1YmxpY05vdGlmaWNhdGlvbhIYCgdjb250ZW50GAEgAigJUgdjb250ZW50KikKCUNvbG9yVHlwZRIHCgNSRUQQABIICgRCTFVFEAESCQoFR1JFRU4QAjKvAQoERWNobxJOCgVIZWxsbxITLmVjaG8yLkhlbGxvUmVxdWVzdBoULmVjaG8yLkhlbGxvUmVzcG9uc2UiGoLT5JMCFDoBKiIPL2FwaS9lY2hvL2hlbGxvElcKC0hlbGxvU3RyZWFtEhMuZWNobzIuSGVsbG9SZXF1ZXN0GhQuZWNobzIuSGVsbG9SZXNwb25zZSIbgtPkkwIVOgEqIhAvYXBpL2VjaG8vc3RyZWFtMAFCJ1olZ2l0aHViLmNvbS9qdWxpYW9ncmlzL2d1cHB5L3BrZy9lY2hvMgqoAgocZ29vZ2xlL2FwaS9hbm5vdGF0aW9ucy5wcm90bxIKZ29vZ2xlLmFwaRoVZ29vZ2xlL2FwaS9odHRwLnByb3RvGiBnb29nbGUvcHJvdG9idWYvZGVzY3JpcHRvci5wcm90bzpLCgRodHRwEh4uZ29vZ2xlLnByb3RvYnVmLk1ldGhvZE9wdGlvbnMYsMq8IiABKAsyFC5nb29nbGUuYXBpLkh0dHBSdWxlUgRodHRwQm4KDmNvbS5nb29nbGUuYXBpQhBBbm5vdGF0aW9uc1Byb3RvUAFaQWdvb2dsZS5nb2xhbmcub3JnL2dlbnByb3RvL2dvb2dsZWFwaXMvYXBpL2Fubm90YXRpb25zO2Fubm90YXRpb25zogIER0FQSWIGcHJvdG8zCuQBChlnb29nbGUvcHJvdG9idWYvYW55LnByb3RvEg9nb29nbGUucHJvdG9idWYiNgoDQW55EhkKCHR5cGVfdXJsGAEgASgJUgd0eXBlVXJsEhQKBXZhbHVlGAIgASgMUgV2YWx1ZUJ2ChNjb20uZ29vZ2xlLnByb3RvYnVmQghBbnlQcm90b1ABWixnb29nbGUuZ29sYW5nLm9yZy9wcm90b2J1Zi90eXBlcy9rbm93bi9hbnlwYqICA0dQQqoCHkdvb2dsZS5Qcm90b2J1Zi5XZWxsS25vd25UeXBlc2IGcHJvdG8zCqwFChVnb29nbGUvYXBpL2h0dHAucHJvdG8SCmdvb2dsZS5hcGkieQoESHR0cBIqCgVydWxlcxgBIAMoCzIULmdvb2dsZS5hcGkuSHR0cFJ1bGVSBXJ1bGVzEkUKH2Z1bGx5X2RlY29kZV9yZXNlcnZlZF9leHBhbnNpb24YAiABKAhSHGZ1bGx5RGVjb2RlUmVzZXJ2ZWRFeHBhbnNpb24i2gIKCEh0dHBSdWxlEhoKCHNlbGVjdG9yGAEgASgJUghzZWxlY3RvchISCgNnZXQYAiABKAlIAFIDZ2V0EhIKA3B1dBgDIAEoCUgAUgNwdXQSFAoEcG9zdBgEIAEoCUgAUgRwb3N0EhgKBmRlbGV0ZRgFIAEoCUgAUgZkZWxldGUSFgoFcGF0Y2gYBiABKAlIAFIFcGF0Y2gSNwoGY3VzdG9tGAggASgLMh0uZ29vZ2xlLmFwaS5DdXN0b21IdHRwUGF0dGVybkgAUgZjdXN0b20SEgoEYm9keRgHIAEoCVIEYm9keRIjCg1yZXNwb25zZV9ib2R5GAwgASgJUgxyZXNwb25zZUJvZHkSRQoTYWRkaXRpb25hbF9iaW5kaW5ncxgLIAMoCzIULmdvb2dsZS5hcGkuSHR0cFJ1bGVSEmFkZGl0aW9uYWxCaW5kaW5nc0IJCgdwYXR0ZXJuIjsKEUN1c3RvbUh0dHBQYXR0ZXJuEhIKBGtpbmQYASABKAlSBGtpbmQSEgoEcGF0aBgCIAEoCVIEcGF0aEJqCg5jb20uZ29vZ2xlLmFwaUIJSHR0cFByb3RvUAFaQWdvb2dsZS5nb2xhbmcub3JnL2dlbnByb3RvL2dvb2dsZWFwaXMvYXBpL2Fubm90YXRpb25zO2Fubm90YXRpb25z+AEBogIER0FQSWIGcHJvdG8zCrE7CiBnb29nbGUvcHJvdG9idWYvZGVzY3JpcHRvci5wcm90bxIPZ29vZ2xlLnByb3RvYnVmIk0KEUZpbGVEZXNjcmlwdG9yU2V0EjgKBGZpbGUYASADKAsyJC5nb29nbGUucHJvdG9idWYuRmlsZURlc2NyaXB0b3JQcm90b1IEZmlsZSLkBAoTRmlsZURlc2NyaXB0b3JQcm90bxISCgRuYW1lGAEgASgJUgRuYW1lEhgKB3BhY2thZ2UYAiABKAlSB3BhY2thZ2USHgoKZGVwZW5kZW5jeRgDIAMoCVIKZGVwZW5kZW5jeRIrChFwdWJsaWNfZGVwZW5kZW5jeRgKIAMoBVIQcHVibGljRGVwZW5kZW5jeRInCg93ZWFrX2RlcGVuZGVuY3kYCyADKAVSDndlYWtEZXBlbmRlbmN5EkMKDG1lc3NhZ2VfdHlwZRgEIAMoCzIgLmdvb2dsZS5wcm90b2J1Zi5EZXNjcmlwdG9yUHJvdG9SC21lc3NhZ2VUeXBlEkEKCWVudW1fdHlwZRgFIAMoCzIkLmdvb2dsZS5wcm90b2J1Zi5FbnVtRGVzY3JpcHRvclByb3RvUghlbnVtVHlwZRJBCgdzZXJ2aWNlGAYgAygLMicuZ29vZ2xlLnByb3RvYnVmLlNlcnZpY2VEZXNjcmlwdG9yUHJvdG9SB3NlcnZpY2USQwoJZXh0ZW5zaW9uGAcgAygLMiUuZ29vZ2xlLnByb3RvYnVmLkZpZWxkRGVzY3JpcHRvclByb3RvUglleHRlbnNpb24SNgoHb3B0aW9ucxgIIAEoCzIcLmdvb2dsZS5wcm90b2J1Zi5GaWxlT3B0aW9uc1IHb3B0aW9ucxJJChBzb3VyY2VfY29kZV9pbmZvGAkgASgLMh8uZ29vZ2xlLnByb3RvYnVmLlNvdXJjZUNvZGVJbmZvUg5zb3VyY2VDb2RlSW5mbxIWCgZzeW50YXgYDCABKAlSBnN5bnRheCK5BgoPRGVzY3JpcHRvclByb3RvEhIKBG5hbWUYASABKAlSBG5hbWUSOwoFZmllbGQYAiADKAsyJS5nb29nbGUucHJvdG9idWYuRmllbGREZXNjcmlwdG9yUHJvdG9SBWZpZWxkEkMKCWV4dGVuc2lvbhgGIAMoCzIlLmdvb2dsZS5wcm90b2J1Zi5GaWVsZERlc2NyaXB0b3JQcm90b1IJZXh0ZW5zaW9uEkEKC25lc3RlZF90eXBlGAMgAygLMiAuZ29vZ2xlLnByb3RvYnVmLkRlc2NyaXB0b3JQcm90b1IKbmVzdGVkVHlwZRJBCgllbnVtX3R5cGUYBCADKAsyJC5nb29nbGUucHJvdG9idWYuRW51bURlc2NyaXB0b3JQcm90b1IIZW51bVR5cGUSWAoPZXh0ZW5zaW9uX3JhbmdlGAUgAygLMi8uZ29vZ2xlLnByb3RvYnVmLkRlc2NyaXB0b3JQcm90by5FeHRlbnNpb25SYW5nZVIOZXh0ZW5zaW9uUmFuZ2USRAoKb25lb2ZfZGVjbBgIIAMoCzIlLmdvb2dsZS5wcm90b2J1Zi5PbmVvZkRlc2NyaXB0b3JQcm90b1IJb25lb2ZEZWNsEjkKB29wdGlvbnMYByABKAsyHy5nb29nbGUucHJvdG9idWYuTWVzc2FnZU9wdGlvbnNSB29wdGlvbnMSVQoOcmVzZXJ2ZWRfcmFuZ2UYCSADKAsyLi5nb29nbGUucHJvdG9idWYuRGVzY3JpcHRvclByb3RvLlJlc2VydmVkUmFuZ2VSDXJlc2VydmVkUmFuZ2USIwoNcmVzZXJ2ZWRfbmFtZRgKIAMoCVIMcmVzZXJ2ZWROYW1lGnoKDkV4dGVuc2lvblJhbmdlEhQKBXN0YXJ0GAEgASgFUgVzdGFydBIQCgNlbmQYAiABKAVSA2VuZBJACgdvcHRpb25zGAMgASgLMiYuZ29vZ2xlLnByb3RvYnVmLkV4dGVuc2lvblJhbmdlT3B0aW9uc1IHb3B0aW9ucxo3Cg1SZXNlcnZlZFJhbmdlEhQKBXN0YXJ0GAEgASgFUgVzdGFydBIQCgNlbmQYAiABKAVSA2VuZCJ8ChVFeHRlbnNpb25SYW5nZU9wdGlvbnMSWAoUdW5pbnRlcnByZXRlZF9vcHRpb24Y5wcgAygLMiQuZ29vZ2xlLnByb3RvYnVmLlVuaW50ZXJwcmV0ZWRPcHRpb25SE3VuaW50ZXJwcmV0ZWRPcHRpb24qCQjoBxCAgICAAiLBBgoURmllbGREZXNjcmlwdG9yUHJvdG8SEgoEbmFtZRgBIAEoCVIEbmFtZRIWCgZudW1iZXIYAyABKAVSBm51bWJlchJBCgVsYWJlbBgEIAEoDjIrLmdvb2dsZS5wcm90b2J1Zi5GaWVsZERlc2NyaXB0b3JQcm90by5MYWJlbFIFbGFiZWwSPgoEdHlwZRgFIAEoDjIqLmdvb2dsZS5wcm90b2J1Zi5GaWVsZERlc2NyaXB0b3JQcm90by5UeXBlUgR0eXBlEhsKCXR5cGVfbmFtZRgGIAEoCVIIdHlwZU5hbWUSGgoIZXh0ZW5kZWUYAiABKAlSCGV4dGVuZGVlEiMKDWRlZmF1bHRfdmFsdWUYByABKAlSDGRlZmF1bHRWYWx1ZRIfCgtvbmVvZl9pbmRleBgJIAEoBVIKb25lb2ZJbmRleBIbCglqc29uX25hbWUYCiABKAlSCGpzb25OYW1lEjcKB29wdGlvbnMYCCABKAsyHS5nb29nbGUucHJvdG9idWYuRmllbGRPcHRpb25zUgdvcHRpb25zEicKD3Byb3RvM19vcHRpb25hbBgRIAEoCFIOcHJvdG8zT3B0aW9uYWwitgIKBFR5cGUSDwoLVFlQRV9ET1VCTEUQARIOCgpUWVBFX0ZMT0FUEAISDgoKVFlQRV9JTlQ2NBADEg8KC1RZUEVfVUlOVDY0EAQSDgoKVFlQRV9JTlQzMhAFEhAKDFRZUEVfRklYRUQ2NBAGEhAKDFRZUEVfRklYRUQzMhAHEg0KCVRZUEVfQk9PTBAIEg8KC1RZUEVfU1RSSU5HEAkSDgoKVFlQRV9HUk9VUBAKEhAKDFRZUEVfTUVTU0FHRRALEg4KClRZUEVfQllURVMQDBIPCgtUWVBFX1VJTlQzMhANEg0KCVRZUEVfRU5VTRAOEhEKDVRZUEVfU0ZJWEVEMzIQDxIRCg1UWVBFX1NGSVhFRDY0EBASDwoLVFlQRV9TSU5UMzIQERIPCgtUWVBFX1NJTlQ2NBASIkMKBUxhYmVsEhIKDkxBQkVMX09QVElPTkFMEAESEgoOTEFCRUxfUkVRVUlSRUQQAhISCg5MQUJFTF9SRVBFQVRFRBADImMKFE9uZW9mRGVzY3JpcHRvclByb3RvEhIKBG5hbWUYASABKAlSBG5hbWUSNwoHb3B0aW9ucxgCIAEoCzIdLmdvb2dsZS5wcm90b2J1Zi5PbmVvZk9wdGlvbnNSB29wdGlvbnMi4wIKE0VudW1EZXNjcmlwdG9yUHJvdG8SEgoEbmFtZRgBIAEoCVIEbmFtZRI/CgV2YWx1ZRgCIAMoCzIpLmdvb2dsZS5wcm90b2J1Zi5FbnVtVmFsdWVEZXNjcmlwdG9yUHJvdG9SBXZhbHVlEjYKB29wdGlvbnMYAyABKAsyHC5nb29nbGUucHJvdG9idWYuRW51bU9wdGlvbnNSB29wdGlvbnMSXQoOcmVzZXJ2ZWRfcmFuZ2UYBCADKAsyNi5nb29nbGUucHJvdG9idWYuRW51bURlc2NyaXB0b3JQcm90by5FbnVtUmVzZXJ2ZWRSYW5nZVINcmVzZXJ2ZWRSYW5nZRIjCg1yZXNlcnZlZF9uYW1lGAUgAygJUgxyZXNlcnZlZE5hbWUaOwoRRW51bVJlc2VydmVkUmFuZ2USFAoFc3RhcnQYASABKAVSBXN0YXJ0EhAKA2VuZBgCIAEoBVIDZW5kIoMBChhFbnVtVmFsdWVEZXNjcmlwdG9yUHJvdG8SEgoEbmFtZRgBIAEoCVIEbmFtZRIWCgZudW1iZXIYAiABKAVSBm51bWJlchI7CgdvcHRpb25zGAMgASgLMiEuZ29vZ2xlLnByb3RvYnVmLkVudW1WYWx1ZU9wdGlvbnNSB29wdGlvbnMipwEKFlNlcnZpY2VEZXNjcmlwdG9yUHJvdG8SEgoEbmFtZRgBIAEoCVIEbmFtZRI+CgZtZXRob2QYAiADKAsyJi5nb29nbGUucHJvdG9idWYuTWV0aG9kRGVzY3JpcHRvclByb3RvUgZtZXRob2QSOQoHb3B0aW9ucxgDIAEoCzIfLmdvb2dsZS5wcm90b2J1Zi5TZXJ2aWNlT3B0aW9uc1IHb3B0aW9ucyKJAgoVTWV0aG9kRGVzY3JpcHRvclByb3RvEhIKBG5hbWUYASABKAlSBG5hbWUSHQoKaW5wdXRfdHlwZRgCIAEoCVIJaW5wdXRUeXBlEh8KC291dHB1dF90eXBlGAMgASgJUgpvdXRwdXRUeXBlEjgKB29wdGlvbnMYBCABKAsyHi5nb29nbGUucHJvdG9idWYuTWV0aG9kT3B0aW9uc1IHb3B0aW9ucxIwChBjbGllbnRfc3RyZWFtaW5nGAUgASgIOgVmYWxzZVIPY2xpZW50U3RyZWFtaW5nEjAKEHNlcnZlcl9zdHJlYW1pbmcYBiABKAg6BWZhbHNlUg9zZXJ2ZXJTdHJlYW1pbmcikQkKC0ZpbGVPcHRpb25zEiEKDGphdmFfcGFja2FnZRgBIAEoCVILamF2YVBhY2thZ2USMAoUamF2YV9vdXRlcl9jbGFzc25hbWUYCCABKAlSEmphdmFPdXRlckNsYXNzbmFtZRI1ChNqYXZhX211bHRpcGxlX2ZpbGVzGAogASgIOgVmYWxzZVIRamF2YU11bHRpcGxlRmlsZXMSRAodamF2YV9nZW5lcmF0ZV9lcXVhbHNfYW5kX2hhc2gYFCABKAhCAhgBUhlqYXZhR2VuZXJhdGVFcXVhbHNBbmRIYXNoEjoKFmphdmFfc3RyaW5nX2NoZWNrX3V0ZjgYGyABKAg6BWZhbHNlUhNqYXZhU3RyaW5nQ2hlY2tVdGY4ElMKDG9wdGltaXplX2ZvchgJIAEoDjIpLmdvb2dsZS5wcm90b2J1Zi5GaWxlT3B0aW9ucy5PcHRpbWl6ZU1vZGU6BVNQRUVEUgtvcHRpbWl6ZUZvchIdCgpnb19wYWNrYWdlGAsgASgJUglnb1BhY2thZ2USNQoTY2NfZ2VuZXJpY19zZXJ2aWNlcxgQIAEoCDoFZmFsc2VSEWNjR2VuZXJpY1NlcnZpY2VzEjkKFWphdmFfZ2VuZXJpY19zZXJ2aWNlcxgRIAEoCDoFZmFsc2VSE2phdmFHZW5lcmljU2VydmljZXMSNQoTcHlfZ2VuZXJpY19zZXJ2aWNlcxgSIAEoCDoFZmFsc2VSEXB5R2VuZXJpY1NlcnZpY2VzEjcKFHBocF9nZW5lcmljX3NlcnZpY2VzGCogASgIOgVmYWxzZVIScGhwR2VuZXJpY1NlcnZpY2VzEiUKCmRlcHJlY2F0ZWQYFyABKAg6BWZhbHNlUgpkZXByZWNhdGVkEi4KEGNjX2VuYWJsZV9hcmVuYXMYHyABKAg6BHRydWVSDmNjRW5hYmxlQXJlbmFzEioKEW9iamNfY2xhc3NfcHJlZml4GCQgASgJUg9vYmpjQ2xhc3NQcmVmaXgSKQoQY3NoYXJwX25hbWVzcGFjZRglIAEoCVIPY3NoYXJwTmFtZXNwYWNlEiEKDHN3aWZ0X3ByZWZpeBgnIAEoCVILc3dpZnRQcmVmaXgSKAoQcGhwX2NsYXNzX3ByZWZpeBgoIAEoCVIOcGhwQ2xhc3NQcmVmaXgSIwoNcGhwX25hbWVzcGFjZRgpIAEoCVIMcGhwTmFtZXNwYWNlEjQKFnBocF9tZXRhZGF0YV9uYW1lc3BhY2UYLCABKAlSFHBocE1ldGFkYXRhTmFtZXNwYWNlEiEKDHJ1YnlfcGFja2FnZRgtIAEoCVILcnVieVBhY2thZ2USWAoUdW5pbnRlcnByZXRlZF9vcHRpb24Y5wcgAygLMiQuZ29vZ2xlLnByb3RvYnVmLlVuaW50ZXJwcmV0ZWRPcHRpb25SE3VuaW50ZXJwcmV0ZWRPcHRpb24iOgoMT3B0aW1pemVNb2RlEgkKBVNQRUVEEAESDQoJQ09ERV9TSVpFEAISEAoMTElURV9SVU5USU1FEAMqCQjoBxCAgICAAkoECCYQJyLRAgoOTWVzc2FnZU9wdGlvbnMSPAoXbWVzc2FnZV9zZXRfd2lyZV9mb3JtYXQYASABKAg6BWZhbHNlUhRtZXNzYWdlU2V0V2lyZUZvcm1hdBJMCh9ub19zdGFuZGFyZF9kZXNjcmlwdG9yX2FjY2Vzc29yGAIgASgIOgVmYWxzZVIcbm9TdGFuZGFyZERlc2NyaXB0b3JBY2Nlc3NvchIlCgpkZXByZWNhdGVkGAMgASgIOgVmYWxzZVIKZGVwcmVjYXRlZBIbCgltYXBfZW50cnkYByABKAhSCG1hcEVudHJ5ElgKFHVuaW50ZXJwcmV0ZWRfb3B0aW9uGOcHIAMoCzIkLmdvb2dsZS5wcm90b2J1Zi5VbmludGVycHJldGVkT3B0aW9uUhN1bmludGVycHJldGVkT3B0aW9uKgkI6AcQgICAgAJKBAgIEAlKBAgJEAoi4gMKDEZpZWxkT3B0aW9ucxJBCgVjdHlwZRgBIAEoDjIjLmdvb2dsZS5wcm90b2J1Zi5GaWVsZE9wdGlvbnMuQ1R5cGU6BlNUUklOR1IFY3R5cGUSFgoGcGFja2VkGAIgASgIUgZwYWNrZWQSRwoGanN0eXBlGAYgASgOMiQuZ29vZ2xlLnByb3RvYnVmLkZpZWxkT3B0aW9ucy5KU1R5cGU6CUpTX05PUk1BTFIGanN0eXBlEhkKBGxhenkYBSABKAg6BWZhbHNlUgRsYXp5EiUKCmRlcHJlY2F0ZWQYAyABKAg6BWZhbHNlUgpkZXByZWNhdGVkEhkKBHdlYWsYCiABKAg6BWZhbHNlUgR3ZWFrElgKFHVuaW50ZXJwcmV0ZWRfb3B0aW9uGOcHIAMoCzIkLmdvb2dsZS5wcm90b2J1Zi5VbmludGVycHJldGVkT3B0aW9uUhN1bmludGVycHJldGVkT3B0aW9uIi8KBUNUeXBlEgoKBlNUUklORxAAEggKBENPUkQQARIQCgxTVFJJTkdfUElFQ0UQAiI1CgZKU1R5cGUSDQoJSlNfTk9STUFMEAASDQoJSlNfU1RSSU5HEAESDQoJSlNfTlVNQkVSEAIqCQjoBxCAgICAAkoECAQQBSJzCgxPbmVvZk9wdGlvbnMSWAoUdW5pbnRlcnByZXRlZF9vcHRpb24Y5wcgAygLMiQuZ29vZ2xlLnByb3RvYnVmLlVuaW50ZXJwcmV0ZWRPcHRpb25SE3VuaW50ZXJwcmV0ZWRPcHRpb24qCQjoBxCAgICAAiLAAQoLRW51bU9wdGlvbnMSHwoLYWxsb3dfYWxpYXMYAiABKAhSCmFsbG93QWxpYXMSJQoKZGVwcmVjYXRlZBgDIAEoCDoFZmFsc2VSCmRlcHJlY2F0ZWQSWAoUdW5pbnRlcnByZXRlZF9vcHRpb24Y5wcgAygLMiQuZ29vZ2xlLnByb3RvYnVmLlVuaW50ZXJwcmV0ZWRPcHRpb25SE3VuaW50ZXJwcmV0ZWRPcHRpb24qCQjoBxCAgICAAkoECAUQBiKeAQoQRW51bVZhbHVlT3B0aW9ucxIlCgpkZXByZWNhdGVkGAEgASgIOgVmYWxzZVIKZGVwcmVjYXRlZBJYChR1bmludGVycHJldGVkX29wdGlvbhjnByADKAsyJC5nb29nbGUucHJvdG9idWYuVW5pbnRlcnByZXRlZE9wdGlvblITdW5pbnRlcnByZXRlZE9wdGlvbioJCOgHEICAgIACIpwBCg5TZXJ2aWNlT3B0aW9ucxIlCgpkZXByZWNhdGVkGCEgASgIOgVmYWxzZVIKZGVwcmVjYXRlZBJYChR1bmludGVycHJldGVkX29wdGlvbhjnByADKAsyJC5nb29nbGUucHJvdG9idWYuVW5pbnRlcnByZXRlZE9wdGlvblITdW5pbnRlcnByZXRlZE9wdGlvbioJCOgHEICAgIACIuACCg1NZXRob2RPcHRpb25zEiUKCmRlcHJlY2F0ZWQYISABKAg6BWZhbHNlUgpkZXByZWNhdGVkEnEKEWlkZW1wb3RlbmN5X2xldmVsGCIgASgOMi8uZ29vZ2xlLnByb3RvYnVmLk1ldGhvZE9wdGlvbnMuSWRlbXBvdGVuY3lMZXZlbDoTSURFTVBPVEVOQ1lfVU5LTk9XTlIQaWRlbXBvdGVuY3lMZXZlbBJYChR1bmludGVycHJldGVkX29wdGlvbhjnByADKAsyJC5nb29nbGUucHJvdG9idWYuVW5pbnRlcnByZXRlZE9wdGlvblITdW5pbnRlcnByZXRlZE9wdGlvbiJQChBJZGVtcG90ZW5jeUxldmVsEhcKE0lERU1QT1RFTkNZX1VOS05PV04QABITCg9OT19TSURFX0VGRkVDVFMQARIOCgpJREVNUE9URU5UEAIqCQjoBxCAgICAAiKaAwoTVW5pbnRlcnByZXRlZE9wdGlvbhJBCgRuYW1lGAIgAygLMi0uZ29vZ2xlLnByb3RvYnVmLlVuaW50ZXJwcmV0ZWRPcHRpb24uTmFtZVBhcnRSBG5hbWUSKQoQaWRlbnRpZmllcl92YWx1ZRgDIAEoCVIPaWRlbnRpZmllclZhbHVlEiwKEnBvc2l0aXZlX2ludF92YWx1ZRgEIAEoBFIQcG9zaXRpdmVJbnRWYWx1ZRIsChJuZWdhdGl2ZV9pbnRfdmFsdWUYBSABKANSEG5lZ2F0aXZlSW50VmFsdWUSIQoMZG91YmxlX3ZhbHVlGAYgASgBUgtkb3VibGVWYWx1ZRIhCgxzdHJpbmdfdmFsdWUYByABKAxSC3N0cmluZ1ZhbHVlEicKD2FnZ3JlZ2F0ZV92YWx1ZRgIIAEoCVIOYWdncmVnYXRlVmFsdWUaSgoITmFtZVBhcnQSGwoJbmFtZV9wYXJ0GAEgAigJUghuYW1lUGFydBIhCgxpc19leHRlbnNpb24YAiACKAhSC2lzRXh0ZW5zaW9uIqcCCg5Tb3VyY2VDb2RlSW5mbxJECghsb2NhdGlvbhgBIAMoCzIoLmdvb2dsZS5wcm90b2J1Zi5Tb3VyY2VDb2RlSW5mby5Mb2NhdGlvblIIbG9jYXRpb24azgEKCExvY2F0aW9uEhYKBHBhdGgYASADKAVCAhABUgRwYXRoEhYKBHNwYW4YAiADKAVCAhABUgRzcGFuEikKEGxlYWRpbmdfY29tbWVudHMYAyABKAlSD2xlYWRpbmdDb21tZW50cxIrChF0cmFpbGluZ19jb21tZW50cxgEIAEoCVIQdHJhaWxpbmdDb21tZW50cxI6ChlsZWFkaW5nX2RldGFjaGVkX2NvbW1lbnRzGAYgAygJUhdsZWFkaW5nRGV0YWNoZWRDb21tZW50cyLRAQoRR2VuZXJhdGVkQ29kZUluZm8STQoKYW5ub3RhdGlvbhgBIAMoCzItLmdvb2dsZS5wcm90b2J1Zi5HZW5lcmF0ZWRDb2RlSW5mby5Bbm5vdGF0aW9uUgphbm5vdGF0aW9uGm0KCkFubm90YXRpb24SFgoEcGF0aBgBIAMoBUICEAFSBHBhdGgSHwoLc291cmNlX2ZpbGUYAiABKAlSCnNvdXJjZUZpbGUSFAoFYmVnaW4YAyABKAVSBWJlZ2luEhAKA2VuZBgEIAEoBVIDZW5kQn4KE2NvbS5nb29nbGUucHJvdG9idWZCEERlc2NyaXB0b3JQcm90b3NIAVotZ29vZ2xlLmdvbGFuZy5vcmcvcHJvdG9idWYvdHlwZXMvZGVzY3JpcHRvcnBi+AEBogIDR1BCqgIaR29vZ2xlLlByb3RvYnVmLlJlZmxlY3Rpb24="
  @Test
  fun `file_containing_symbol service`() {
    val schema = loadSchema()
    val expectedResponse = ServerReflectionResponse.ADAPTER.decode(ECHO2_FILEDESCRIPTOR_RESPONSE.decodeBase64()!!)
    val response = SchemaReflector(schema, includeDependencies = false).process(
      ServerReflectionRequest(
        file_containing_symbol = "echo2.Echo",
        host = "localhost:0"
      )
    )
    val unwantedValueStripper = UnwantedValueStripper(clearJsonName = true)
    assertThat(unwantedValueStripper.stripOptionsAndDefaults(response.fileDescriptors[0]))
      .isEqualTo(unwantedValueStripper.stripOptionsAndDefaults(expectedResponse.fileDescriptors[0]))
  }

  @Test
  fun `file_containing_symbol message`() {
    val schema = loadSchema()
    val expectedResponse = ServerReflectionResponse.ADAPTER.decode(ECHO2_FILEDESCRIPTOR_RESPONSE.decodeBase64()!!)
    val response = SchemaReflector(schema, includeDependencies = false).process(
      ServerReflectionRequest(
        file_containing_symbol = "echo2.HelloRequest",
        host = "localhost:0"
      )
    )
    val unwantedValueStripper = UnwantedValueStripper(clearJsonName = true)
    assertThat(unwantedValueStripper.stripOptionsAndDefaults(response.fileDescriptors[0]))
      .isEqualTo(unwantedValueStripper.stripOptionsAndDefaults(expectedResponse.fileDescriptors[0]))
  }

  @Test
  fun `file_containing_symbol rpc`() {
    val schema = loadSchema()
    val expectedResponse = ServerReflectionResponse.ADAPTER.decode(ECHO2_FILEDESCRIPTOR_RESPONSE.decodeBase64()!!)
    val response = SchemaReflector(schema, includeDependencies = false).process(
      ServerReflectionRequest(
        file_containing_symbol = "echo2.Echo.HelloStream",
        host = "localhost:0"
      )
    )
    val unwantedValueStripper = UnwantedValueStripper(clearJsonName = true)
    assertThat(unwantedValueStripper.stripOptionsAndDefaults(response.fileDescriptors[0]))
      .isEqualTo(unwantedValueStripper.stripOptionsAndDefaults(expectedResponse.fileDescriptors[0]))
  }

  @Test
  fun `file_by_filename`() {
    val schema = loadSchema()
    val expectedResponse = ServerReflectionResponse.ADAPTER.decode(ECHO2_FILEDESCRIPTOR_RESPONSE.decodeBase64()!!)
    val response = SchemaReflector(schema, includeDependencies = false).process(
      ServerReflectionRequest(
        file_by_filename = "echo2/echo2.proto",
        host = "localhost:0"
      )
    )
    val unwantedValueStripper = UnwantedValueStripper(clearJsonName = true)
    assertThat(unwantedValueStripper.stripOptionsAndDefaults(response.fileDescriptors[0]))
      .isEqualTo(unwantedValueStripper.stripOptionsAndDefaults(expectedResponse.fileDescriptors[0]))
  }

  private val ServerReflectionResponse.fileDescriptors
    get() = file_descriptor_response!!.file_descriptor_proto.map {
      FileDescriptorProto.parseFrom(
        it.toByteArray()
      )
    }
}

private fun loadSchema(): Schema {
  return SchemaLoader(FileSystem.SYSTEM)
    .apply {
      initRoots(
        // TODO(juliaogris): Can we derive dependencies transitively?
        sourcePath = listOf(
          Location.get("src/main/resources", "grpc/reflection/v1alpha/reflection.proto"),
          Location.get("src/test/proto", "echo2/echo2.proto"),
          Location.get("src/test/proto", "google/api/annotations.proto"),
          Location.get("src/test/proto", "google/api/http.proto"),
          Location.get("src/test/proto", "google/protobuf/any.proto"),
        ),
        protoPath = listOf()
      )
    }
    .loadSchema()
}
