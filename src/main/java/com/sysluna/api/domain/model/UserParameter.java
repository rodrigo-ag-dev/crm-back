package com.sysluna.api.domain.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "parameter_user", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "user_id", "parameter_id" })
})
public class UserParameter extends BaseModel {

  @Column(name = "user_id", nullable = false)
  private String userId;

  @Column(name = "parameter_id", nullable = false)
  private String parameterId;

  @Column(nullable = false)
  private String value;

  @ManyToOne
  @JoinColumn(name = "user_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_parameter_user_user"))
  private User user;

  @ManyToOne
  @JoinColumn(name = "parameter_id", insertable = false, updatable = false, foreignKey = @ForeignKey(name = "fk_parameter_user_parameter"))
  private Parameter parameter;
}
