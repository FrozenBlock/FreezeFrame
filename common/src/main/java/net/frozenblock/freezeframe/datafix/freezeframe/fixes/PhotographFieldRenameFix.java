package net.frozenblock.freezeframe.datafix.freezeframe.fixes;

import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import java.util.Optional;
import net.frozenblock.freezeframe.FFConstants;
import net.minecraft.util.datafix.fixes.References;

public final class PhotographFieldRenameFix extends DataFix {
	private final String name;
	private final String oldFieldName;
	private final String newFieldName;

	public PhotographFieldRenameFix(String name, String oldFieldName, String newFieldName, Schema outputSchema) {
		super(outputSchema, false);
		this.name = name;
		this.oldFieldName = oldFieldName;
		this.newFieldName = newFieldName;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return this.writeFixAndRead(
			this.name,
			this.getInputSchema().getType(References.DATA_COMPONENTS),
			this.getOutputSchema().getType(References.DATA_COMPONENTS),
			components -> {
				final Optional<? extends Dynamic<?>> photographComponent = components.get(FFConstants.string("photograph")).result();
				if (photographComponent.isEmpty()) return components;

				final Dynamic<?> fixedPhotographComponent = photographComponent.get().renameField(this.oldFieldName, this.newFieldName);
				return components.set(FFConstants.string("photograph"), fixedPhotographComponent);
			}
		);
	}
}
