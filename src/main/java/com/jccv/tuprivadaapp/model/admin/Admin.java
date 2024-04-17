package com.jccv.tuprivadaapp.model.admin;

import com.jccv.tuprivadaapp.model.User;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity
@Table(name = "admins")
public class Admin extends User {

    private Long code;

}
