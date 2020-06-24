// Code generated by Wire protocol buffer compiler, do not edit.
// Source file: one_of.proto
import Foundation
import Wire

public struct OneOfMessage : Equatable, Proto2Codable, Codable {

    public var choice: Choice?
    public var unknownFields: Data = .init()

    public init(from decoder: Decoder) throws {
        let container = try decoder.container(keyedBy: CodingKeys.self)
        if (container.contains(.foo)) {
            let foo = try container.decode(Int32.self, forKey: .foo)
            self.choice = .foo(foo)
        } else if (container.contains(.bar)) {
            let bar = try container.decode(String.self, forKey: .bar)
            self.choice = .bar(bar)
        } else if (container.contains(.baz)) {
            let baz = try container.decode(String.self, forKey: .baz)
            self.choice = .baz(baz)
        } else {
            self.choice = nil
        }
    }

    public init(choice: Choice? = nil) {
        self.choice = choice
    }

    public init(from reader: ProtoReader) throws {
        var choice: Choice? = nil

        let unknownFields = try reader.forEachTag { tag in
            switch tag {
                case 1: choice = .foo(try reader.decode(Int32.self))
                case 3: choice = .bar(try reader.decode(String.self))
                case 4: choice = .baz(try reader.decode(String.self))
                default: try reader.readUnknownField(tag: tag)
            }
        }

        self.choice = choice
        self.unknownFields = unknownFields
    }

    public func encode(to encoder: Encoder) throws {
        var container = encoder.container(keyedBy: CodingKeys.self)
        switch self.choice {
            case .foo(let foo): try container.encode(foo, forKey: .foo)
            case .bar(let bar): try container.encode(bar, forKey: .bar)
            case .baz(let baz): try container.encode(baz, forKey: .baz)
            case Optional.none: break
        }
    }

    public func encode(to writer: ProtoWriter) throws {
        if let choice = choice {
            try choice.encode(to: writer)
        }
        try writer.writeUnknownFields(unknownFields)
    }

    public enum Choice : Equatable {

        case foo(Int32)
        case bar(String)
        case baz(String)

        fileprivate func encode(to writer: ProtoWriter) throws {
            switch self {
                case .foo(let foo): try writer.encode(tag: 1, value: foo)
                case .bar(let bar): try writer.encode(tag: 3, value: bar)
                case .baz(let baz): try writer.encode(tag: 4, value: baz)
            }
        }

    }

    private enum CodingKeys : String, CodingKey {

        case foo
        case bar
        case baz

    }

}
