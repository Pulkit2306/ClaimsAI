package com.claimsplatform.searchservice.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "claims")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimDocument {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String claimNumber;

    @Field(type = FieldType.Long)
    private Long claimId;

    @Field(type = FieldType.Long)
    private Long policyId;

    @Field(type = FieldType.Long)
    private Long customerId;

    @Field(type = FieldType.Keyword)
    private String claimType;

    @Field(type = FieldType.Keyword)
    private String status;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Double)
    private Double estimatedAmount;

    @Field(type = FieldType.Text)
    private String aiSummary;

    @Field(type = FieldType.Double)
    private Double fraudScore;

    @Field(type = FieldType.Auto)
    private String createdAt;

    @Field(type = FieldType.Auto)
    private String updatedAt;
}
