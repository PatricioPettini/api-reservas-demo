package com.patojunit.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class ReservaAdminGetDTO extends ReservaUserGetDTO{
    private UsuarioGetDTO usuario;
}
