package alicanteweb.pelisapp.entity;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Setter
@Getter
@Embeddable
public class UsuarioRoleId implements Serializable {
    private Long usuario_id;
    private Long role_id;

    public UsuarioRoleId() {}

    public UsuarioRoleId(Long usuario_id, Long role_id) {
        this.usuario_id = usuario_id;
        this.role_id = role_id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UsuarioRoleId that = (UsuarioRoleId) o;
        return Objects.equals(usuario_id, that.usuario_id) && Objects.equals(role_id, that.role_id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(usuario_id, role_id);
    }
}
